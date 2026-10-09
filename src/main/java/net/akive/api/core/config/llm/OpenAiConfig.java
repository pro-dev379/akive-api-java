package net.akive.api.core.config.llm;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAIClient openAIClient(OpenAiProperties openAiProperties) {
        return OpenAIOkHttpClient.builder()
                .apiKey(openAiProperties.apiKey())
                .timeout(openAiProperties.timeout())
                .build();
    }
}
