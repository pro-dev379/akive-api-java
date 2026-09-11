package net.akive.api.auth.application.password.provided.dto;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN;

public record PasswordUpdateCommand(Long userId, String currentPassword, String targetPassword) {

    public PasswordUpdateCommand {
        requirePositive(userId);
        requireNotBlank(currentPassword);
        targetPassword = validatePassword(targetPassword);
    }

    private String validatePassword(String targetPassword) {
        targetPassword = requireNotBlank(targetPassword);
        requireMinLength(targetPassword, PASSWORD_MIN_LENGTH);
        requireMatches(targetPassword, PASSWORD_PATTERN);

        return targetPassword;
    }
}
