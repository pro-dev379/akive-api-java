package net.akive.api.core.config.llm;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        @NotBlank String apiKey,
        @NotNull Duration timeout,
        @NotNull Duration streamingTimeout,
        @NotNull @Valid Model model,
        @NotNull @Valid Embedding embedding
) {

    public record Model(
            @NotBlank String coaching,
            @NotBlank String conversationTitle,
            @NotBlank String learnerInsight,
            @NotBlank String embed
    ) {
    }

    public record Embedding(@Positive int dimensions) {
    }
}
