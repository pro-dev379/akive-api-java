package net.akive.api.core.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.refresh-token-cookie")
public record RefreshTokenCookieProperties(
        @NotBlank @Pattern(regexp = "Strict|Lax|None") String sameSite
) {
}
