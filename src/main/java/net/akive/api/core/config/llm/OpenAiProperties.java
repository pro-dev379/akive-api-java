package net.akive.api.core.config.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        @NotBlank String apiKey,
        @NotNull Duration timeout
) {
}
