package es.uniovi.avib.morphing.projections.backend.storage.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class ObjectStorageConfig {
    @Value("${minio.host}")
    private String host;

    @Value("${minio.port}")
    private int port;
    
    @Value("${minio.disable-tls}")
    private boolean disableTls;
    
    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;
}