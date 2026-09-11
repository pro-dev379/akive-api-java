package net.akive.api.auth.adapter.password.webapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;

import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN_REGEX;

public record PasswordUpdateRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = PASSWORD_MIN_LENGTH)
        @Pattern(regexp = PASSWORD_PATTERN_REGEX)
        String targetPassword
) {

    public PasswordUpdateCommand to(Long userId) {
        return new PasswordUpdateCommand(userId, this.currentPassword, this.targetPassword);
    }
}
