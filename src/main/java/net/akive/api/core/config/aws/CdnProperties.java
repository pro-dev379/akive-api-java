package net.akive.api.core.config.aws;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "image.cdn")
public record CdnProperties(@NotNull URI baseUrl) {

    public URI resolve(String objectKey) {
        String normalizedBaseUrl = this.baseUrl.toString().replaceAll("/+$", "");
        String normalizedObjectKey = objectKey.replaceFirst("^/+", "");

        return URI.create(normalizedBaseUrl + "/" + normalizedObjectKey);
    }
}
