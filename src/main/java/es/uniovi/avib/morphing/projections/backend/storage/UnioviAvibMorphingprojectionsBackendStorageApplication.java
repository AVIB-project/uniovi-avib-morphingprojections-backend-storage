package es.uniovi.avib.morphing.projections.backend.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UnioviAvibMorphingprojectionsBackendStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnioviAvibMorphingprojectionsBackendStorageApplication.class, args);
	}
	
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }	
}
