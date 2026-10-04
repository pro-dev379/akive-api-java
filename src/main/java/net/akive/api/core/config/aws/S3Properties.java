package net.akive.api.core.config.aws;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "image.storage.s3")
public record S3Properties(@NotBlank String bucketName) {
}
