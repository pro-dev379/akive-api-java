package net.akive.api.user.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.domain.RoleType;

public record SignUpByEmailRequest(
        @Email
        @NotBlank
        String email,

        @NotNull
        RoleType roleType,

        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).*$")
        String password
) {
        public SignUpByEmailCommand to() {
                return SignUpByEmailCommand.of(email, roleType, password);
        }
}
