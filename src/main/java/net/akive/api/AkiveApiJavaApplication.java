package net.akive.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableJpaAuditing
@EnableRedisRepositories(redisTemplateRef = "customRedisTemplate")
@SpringBootApplication
public class AkiveApiJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AkiveApiJavaApplication.class, args);
	}

}
