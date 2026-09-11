package net.akive.api.auth.application.password.provided.dto;

import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_MIN_LENGTH;
import static net.akive.api.user.domain.UserPolicy.PASSWORD_PATTERN;

public record PasswordResetCommand(Email email, String targetPassword) {

    public PasswordResetCommand {
        requireNonNull(email);
        targetPassword = validatePassword(targetPassword);
    }

    public static PasswordResetCommand of(String email, String targetPassword) {
        return new PasswordResetCommand(new Email(email), targetPassword);
    }

    private String validatePassword(String targetPassword) {
        targetPassword = requireNotBlank(targetPassword);
        requireMinLength(targetPassword, PASSWORD_MIN_LENGTH);
        requireMatches(targetPassword, PASSWORD_PATTERN);

        return targetPassword;
    }
}
