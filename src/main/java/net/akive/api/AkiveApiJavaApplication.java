package net.akive.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class AkiveApiJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AkiveApiJavaApplication.class, args);
	}

}
