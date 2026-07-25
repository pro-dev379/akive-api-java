package net.akive.api.user.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.domain.RoleType;

import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN_REGEX;

public record SignUpByEmailRequest(
        @Email
        @NotBlank
        String email,

        @NotNull
        RoleType roleType,

        @NotBlank
        @Size(min = PASSWORD_MIN_LENGTH)
        @Pattern(regexp = PASSWORD_PATTERN_REGEX)
        String password
) {
        public SignUpByEmailCommand to() {
                return SignUpByEmailCommand.of(email, roleType, password);
        }
}
