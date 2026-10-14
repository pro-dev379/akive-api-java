package net.akive.api.core.config.vector;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "vector.qdrant")
public record QdrantProperties(
        @NotBlank String host,
        @Positive int grpcPort,
        boolean tlsEnabled,
        @NotNull Duration timeout,
        @NotBlank String learnerInsightCollectionName
) {
}
