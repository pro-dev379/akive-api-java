package net.akive.api.image.adapter.scheduling;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "image.cleanup")
public record ImageCleanupProperties(@NotNull Duration retention, @Positive int batchSize) {
}
