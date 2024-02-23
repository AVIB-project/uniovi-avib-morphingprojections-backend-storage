package es.uniovi.avib.morphing.projections.backend.storage.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.minioPort}")
    private int port;
    
    @Value("${minio.secure}")
    private boolean secure;
    
    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient generateMinioClient() throws Exception {
    	try {    	
	        MinioClient client = MinioClient.builder()
	        		.endpoint(endpoint, port, secure)
	                .credentials(accessKey, secretKey)                
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
