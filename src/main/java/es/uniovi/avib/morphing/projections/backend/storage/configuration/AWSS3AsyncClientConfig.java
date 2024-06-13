package es.uniovi.avib.morphing.projections.backend.storage.configuration;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkAsyncHttpClientBuilder;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.utils.AttributeMap;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AWSS3AsyncClientConfig {
	private final ObjectStorageConfig objectStorageConfig;
	
	// disable cert validation
	private final AttributeMap attributeMap = AttributeMap.builder().put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true).build();
	private final SdkAsyncHttpClient sdkAsyncHttpClient = new DefaultSdkAsyncHttpClientBuilder().buildWithDefaults(attributeMap);
	
	@Bean
    public S3AsyncClient generateS3AsyncClient() throws Exception {
		// configure object storage credentials
		StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(
						objectStorageConfig.getAccessKey(),
						objectStorageConfig.getSecretKey()));
		
		return S3AsyncClient.builder()   
        		.endpointOverride(URI.create("https://" + objectStorageConfig.getHost() + ":" + objectStorageConfig.getPort()))
				//.endpointOverride(URI.create("http://" + objectStorageConfig.getHost() + ":" + objectStorageConfig.getPort()))
        		.region(Region.US_EAST_1)
        		.forcePathStyle(true)    
        		.httpClient(sdkAsyncHttpClient)
        		.credentialsProvider(credentials)			
        	.build();		
	}
}
