package es.uniovi.avib.morphing.projections.backend.storage.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CSVInput;
import software.amazon.awssdk.services.s3.model.CompressionType;
import software.amazon.awssdk.services.s3.model.ExpressionType;
import software.amazon.awssdk.services.s3.model.FileHeaderInfo;
import software.amazon.awssdk.services.s3.model.InputSerialization;
import software.amazon.awssdk.services.s3.model.JSONOutput;
import software.amazon.awssdk.services.s3.model.OutputSerialization;
import software.amazon.awssdk.services.s3.model.Progress;
import software.amazon.awssdk.services.s3.model.SelectObjectContentRequest;
import software.amazon.awssdk.services.s3.model.SelectObjectContentResponseHandler;
import software.amazon.awssdk.services.s3.model.Stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.storage.dto.ProjectionRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionService {
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
    
	private SelectObjectContentRequest generateBaseRequest(String bucket, String key, boolean isGzip, String query) {
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
        		.json(JSONOutput.builder().build())        		
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
    
	public List<Object> findProjection(ProjectionRequestDto projectionRequestDto) throws Exception {
		log.debug("findProjection: found projection from resource: {}", projectionRequestDto.getKeyObjectName());
				 				
		List<Object> projections = new ArrayList<Object>();
                  
        // prepare S3 Select Request
		String query = "select s.* from s3object s";
		
		boolean isGzip = false;
		if (projectionRequestDto.getKeyObjectName().contains(".gz"))
			isGzip = true;

        SelectObjectContentRequest select = generateBaseRequest(
        		projectionRequestDto.getBucketName(), 
        		projectionRequestDto.getKeyObjectName(), 
        		isGzip, 
        		query);
                                
        EventStreamInfo eventStreamInfo = new EventStreamInfo();

        // execute S3 Select Request
        s3AsyncClient
        	.selectObjectContent(select, buildResponseHandler(eventStreamInfo))
        	.join();
           
        // get S3 Select Request reponse
        StringBuilder recordSb = new StringBuilder();
        for(String record : eventStreamInfo.getRecords()){
        	recordSb.append(record);           
        }
        
        // parse S3 Select json reponse to generic objects
        Gson gson = new GsonBuilder().setLenient().create();
        
        List<String> recordList = Arrays.asList(recordSb.toString().split("\n"));
                
        recordList.forEach(projection -> {
    		try {
    			projections.add(gson.fromJson(projection, Object.class));
    		} catch (Exception e) {
    			log.error(e.getLocalizedMessage());
			}
    	});
        
		return projections;			
	}	
}
