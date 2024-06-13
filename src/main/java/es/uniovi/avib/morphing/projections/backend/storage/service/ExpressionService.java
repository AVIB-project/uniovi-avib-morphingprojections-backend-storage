package es.uniovi.avib.morphing.projections.backend.storage.service;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CSVInput;
import software.amazon.awssdk.services.s3.model.CSVOutput;
import software.amazon.awssdk.services.s3.model.CompressionType;
import software.amazon.awssdk.services.s3.model.ExpressionType;
import software.amazon.awssdk.services.s3.model.FileHeaderInfo;
import software.amazon.awssdk.services.s3.model.InputSerialization;
import software.amazon.awssdk.services.s3.model.OutputSerialization;
import software.amazon.awssdk.services.s3.model.Progress;
import software.amazon.awssdk.services.s3.model.SelectObjectContentRequest;
import software.amazon.awssdk.services.s3.model.SelectObjectContentResponseHandler;
import software.amazon.awssdk.services.s3.model.Stats;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.storage.dto.ExpressionRequestDto;
import es.uniovi.avib.morphing.projections.backend.storage.domain.Expression;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressionService {
	private final S3AsyncClient s3AsyncClient;
    
    // The EventStreamInfo class is used to store information gathered while processing the response stream.
    @SuppressWarnings("unused")
    private class EventStreamInfo {
        private final List<String> records = new ArrayList<>();
        private Integer countOnRecordsCalled = 0;
        private Integer countContinuationEvents = 0;
        private Stats stats;

        void incrementOnRecordsCalled() {
            countOnRecordsCalled++;
        }

        void incrementContinuationEvents() {
            countContinuationEvents++;
        }

        void addRecord(String record) {
            records.add(record);
        }

        void addStats(Stats stats) {
            this.stats = stats;
        }

        public List<String> getRecords() {
            return records;
        }

		public Integer getCountOnRecordsCalled() {
            return countOnRecordsCalled;
        }

        public Integer getCountContinuationEvents() {
            return countContinuationEvents;
        }

        public Stats getStats() {
            return stats;
        }
    }
    
	private SelectObjectContentRequest generateBaseCSVRequest(String bucket, String key, boolean isGzip, String query) {
        InputSerialization inputSerialization;        
        if (isGzip)
            inputSerialization = InputSerialization.builder()
        		.csv(CSVInput.builder().fileHeaderInfo(FileHeaderInfo.USE).build())
        		.compressionType(CompressionType.GZIP)
        	.build();        	
        else 
            inputSerialization = InputSerialization.builder()
        		.csv(CSVInput.builder().fileHeaderInfo(FileHeaderInfo.USE).build())
        		.compressionType(CompressionType.NONE)
        	.build();
        
        OutputSerialization outputSerialization = OutputSerialization.builder()
        		.csv(CSVOutput.builder().build())
        		.build();      
        
        SelectObjectContentRequest request = SelectObjectContentRequest.builder()
	    	.bucket(bucket)
	    	.key(key)
	    	.expression(query)
	    	.expressionType(ExpressionType.SQL)
	    	.inputSerialization(inputSerialization)
	    	.outputSerialization(outputSerialization)
	    .build();
        
        return request;
    }
    
    private SelectObjectContentResponseHandler buildResponseHandler(EventStreamInfo eventStreamInfo) {
        // Use a Visitor to process the response stream. This visitor logs information and gathers details while processing.
        final SelectObjectContentResponseHandler.Visitor visitor = SelectObjectContentResponseHandler.Visitor.builder()
                .onRecords(r -> {
                    log.info("Record event received.");
                    
                    eventStreamInfo.addRecord(r.payload().asUtf8String());
                    eventStreamInfo.incrementOnRecordsCalled();
                })
                .onCont(ce -> {
                	log.info("Continuation event received.");
                	
                    eventStreamInfo.incrementContinuationEvents();
                })
                .onProgress(pe -> {
                    Progress progress = pe.details();
                    log.info("Progress event received:\n bytesScanned:{}\nbytesProcessed: {}\nbytesReturned:{}",
                            progress.bytesScanned(),
                            progress.bytesProcessed(),
                            progress.bytesReturned());
                })
                .onStats(se -> {
                	log.info("Stats event received.");
                	
                    eventStreamInfo.addStats(se.details());
                })
                .onEnd(ee -> {
                	log.info("End event received.");
                })
                .build();

        // Build the SelectObjectContentResponseHandler with the visitor that processes the stream.
        return SelectObjectContentResponseHandler.builder()
                .subscriber(visitor)
                .build();
    }
        
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> List<T> convertToExpression(InputStream file, Class<T> responseType) {
        List<T> models;
        
        try (Reader reader = new BufferedReader(new InputStreamReader(file))) {           
			CsvToBean<?> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(responseType)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            models = (List<T>) csvToBean.parse();
        } catch (Exception ex) {
            log.error("error parsing csv file {} ", ex);
            throw new IllegalArgumentException(ex.getCause().getMessage());
        }
        
        return models;
    }
           
	public List<Expression> findAllExpressionsByAnnotation(ExpressionRequestDto expressionRequestDto) throws Exception {
		log.debug("findAllExpressionsByAnnotation: found expressions from annotation Id: {}", expressionRequestDto.getAnnotationId());
				 				
		List<Expression> expressions = new ArrayList<Expression>();

        // prepare S3 Select Request
		String query = "select s._1, s.\"" + expressionRequestDto.getAnnotationId() + "\" from s3object s";
		
		boolean isGzip = false;
		if (expressionRequestDto.getKeyObjectName().contains(".gz"))
			isGzip = true;

        SelectObjectContentRequest select = generateBaseCSVRequest(
        		expressionRequestDto.getBucketName(), 
        		expressionRequestDto.getKeyObjectName(), 
        		isGzip, 
        		query);
                                
        EventStreamInfo eventStreamInfo = new EventStreamInfo();

        // execute S3 Select Request
        s3AsyncClient
        	.selectObjectContent(select, buildResponseHandler(eventStreamInfo))
        	.join();
        
        // get S3 Select Request reponse
        StringBuilder sb = new StringBuilder();
        for(String s : eventStreamInfo.getRecords()){
            sb.append(s);           
        }
        
        // parsing async result to stream input and parse        
        ByteArrayInputStream resultInputStream = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        
        expressions = convertToExpression(resultInputStream, Expression.class);
        
		return expressions;			
	}
}
