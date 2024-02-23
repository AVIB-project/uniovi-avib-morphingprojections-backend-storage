package es.uniovi.avib.morphing.projections.backend.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class UnioviAvibMorphingprojectionsBackendStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnioviAvibMorphingprojectionsBackendStorageApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsMappingConfigurer() {
	   return new WebMvcConfigurer() {
	       @Override
	       public void addCorsMappings(CorsRegistry registry) {	           
	           registry.addMapping("/**");
	       }
	   };
	}
	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}	
}
