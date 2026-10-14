package net.akive.api.core.config.aws;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "event.sqs")
public record EventSqsProperties(
        @NotNull @Valid LearnerInsightAnalysis learnerInsightAnalysis,
        @NotNull @Valid LearnerInsightEmbedding learnerInsightEmbedding
) {

    public record LearnerInsightAnalysis(@NotBlank String queueName) {
    }

    public record LearnerInsightEmbedding(@NotBlank String queueName) {
    }
}
