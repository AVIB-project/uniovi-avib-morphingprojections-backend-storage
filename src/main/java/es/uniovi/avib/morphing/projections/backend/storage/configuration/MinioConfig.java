package es.uniovi.avib.morphing.projections.backend.storage.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
	private final ObjectStorageConfig objectStorageConfig;
	
	@Bean
    public MinioClient generateMinioClient() throws Exception {
    	try {    	
	        MinioClient client = MinioClient.builder()
	        		.endpoint(
	        				objectStorageConfig.getHost(),
	        				objectStorageConfig.getPort(),
	        				objectStorageConfig.isDisableTls())
	                .credentials(
	                		objectStorageConfig.getAccessKey(),
	                		objectStorageConfig.getSecretKey())                
	                .build();
	        
	        // ignore the autosigned certificate for TLS connection
	        client.ignoreCertCheck();
	       
	        // make simple request to check the minio connection
	        client.listBuckets();
	        
	        return client;
    	} catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
