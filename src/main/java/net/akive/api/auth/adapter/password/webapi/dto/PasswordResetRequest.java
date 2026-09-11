package net.akive.api.auth.adapter.password.webapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.akive.api.auth.application.password.provided.dto.PasswordResetCommand;

import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN_REGEX;

public record PasswordResetRequest(
        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = PASSWORD_MIN_LENGTH)
        @Pattern(regexp = PASSWORD_PATTERN_REGEX)
        String targetPassword
) {

    public PasswordResetCommand to() {
        return PasswordResetCommand.of(this.email, this.targetPassword);
    }
}
