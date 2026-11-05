package net.akive.api.core.config.observability;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "observability.remote-service-naming")
public record RemoteServiceNamingProperties(
        @NotBlank String database,
        @NotBlank String redis,
        @NotBlank String qdrant
) {
}
