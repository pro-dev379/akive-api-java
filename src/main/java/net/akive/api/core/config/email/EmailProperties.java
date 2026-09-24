package net.akive.api.core.config.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "email")
public record EmailProperties(@NotNull @Valid FromProperties from) {

    public record FromProperties(
            @NotBlank @Email String verification,
            @NotBlank @Email String welcome,
            @NotBlank @Email String support
    ) {
    }
}
