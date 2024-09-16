package es.uniovi.avib.morphing.projections.backend.storage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Bucket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.storage.configuration.OrganizationConfig;
import es.uniovi.avib.morphing.projections.backend.storage.dto.DownloadFileResponse;
import es.uniovi.avib.morphing.projections.backend.storage.dto.ResourceDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
	private final OrganizationConfig organizationConfig;
	private final RestTemplate restTemplate;
	private final MinioClient minioClient;
	
	public List<Bucket> getAllBuckets() {
	   try {
           return minioClient.listBuckets();
       } catch (Exception e) {
           throw new RuntimeException(e.getMessage());
       }
    }
		
    public void uploadFiles(String organizationId, String projectId, String caseId, MultipartFile file) throws Exception {
    	log.debug("uploadFiles: upload file with name: {}", file.getOriginalFilename().toString());
    		
    	try {
    		// check if exist bucket if not created one
    		if (!minioClient.bucketExists(
    				BucketExistsArgs.builder()
                		.bucket(organizationId)                	
                		.build())) {    		
			        minioClient.makeBucket(
			        		MakeBucketArgs.builder()
			                	.bucket(organizationId)                	
			                    .build());
    		}
    	} catch (Exception error) {
    		throw error;                        
        }
    	
        try (InputStream is = file.getInputStream()) {        	   
        	// persist object file
            minioClient.putObject(
            		PutObjectArgs.builder()
                    	.bucket(organizationId)
                    	.object(projectId + "/" + caseId + "/" + file.getOriginalFilename()).stream(is, is.available(), -1)
                        .contentType(file.getContentType())
                        .build());				
        } catch (Exception error) {
            throw error;                       
        }
    }
    
    @SuppressWarnings("resource")
	public List<DownloadFileResponse> downloadFilesbyCaseId(String caseId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
				
		String url = "http://" + organizationConfig.getHost() + ":" + organizationConfig.getPort() + "/resources/cases/" + caseId;
		
    	ResponseEntity<ResourceDto[]> responseEntity = restTemplate.getForEntity(url,  ResourceDto[].class);
    	
    	List<ResourceDto> resources = Arrays.asList(responseEntity.getBody());
    	
    	List<DownloadFileResponse> downloadFilesResponse = new ArrayList<DownloadFileResponse>();    	
        try {
        	for (ResourceDto resource : resources) {
        		String[] parts = resource.getBucket().split("/");
        				
        		String bucket = parts[0];
        		String file = parts[1] + "/" + parts[2] + "/" + resource.getFile();
        		
				InputStream stream = minioClient.getObject(
						  GetObjectArgs.builder()
						  	.bucket(bucket)
						  	.object(file)
						  	.build());
				// A debug function
				byte[] buffer = stream.readAllBytes();
				File targetFile = new File("/home/miguel/temp/resources/test/" + parts[3]);
				OutputStream outStream = new FileOutputStream(targetFile);
				outStream.write(buffer);				
        	}
        } catch (Exception e) {
            throw new RuntimeException("Failed to download files.", e);                       
        }
        
        return downloadFilesResponse;
    }    
    
 	public byte[] downloadFileByFilename(String organizationId, String projectId, String caseId, String filename) throws IOException {
	 	String file = projectId + "/" + caseId + "/" + filename;
	 	
        try {        	 
				InputStream inputStream = minioClient.getObject(
						  GetObjectArgs.builder()
						  	.bucket(organizationId)
						  	.object(file)
						  .build());
			    	
				return inputStream.readAllBytes();				
        } catch (Exception ex) {
             throw new RuntimeException("Failed to download file.", ex);                       
        }
     } 
    
    public boolean deleteFile(String organizationId, String projectId, String caseId, String file) {
    	try {
        	//set bucket and object name to persisted
        	String bucket = organizationId;
        	String object = projectId + "/" + caseId + "/" + file;
        	
        	// persist object file
            minioClient.removeObject(
            		RemoveObjectArgs.builder()
                    	.bucket(bucket)
                    	.object(object)
                        .build());	
    	} catch (Exception e) {
            throw new RuntimeException("Failed to delete file.", e);                       
        }
    	
    	return true;
    }
}
