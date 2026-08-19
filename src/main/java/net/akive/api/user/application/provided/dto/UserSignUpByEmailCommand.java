package net.akive.api.user.application.provided.dto;

import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.*;

public record UserSignUpByEmailCommand(
        Email email,
        String password,
        String nickname
) {

    public UserSignUpByEmailCommand {
        requireNonNull(email);
        password = validatePassword(password);
        requireMaxLengthIfPresent(nickname, NICKNAME_MAX_LENGTH);
    }

    public static UserSignUpByEmailCommand of(String email, String password, String nickname) {
        return new UserSignUpByEmailCommand(new Email(email), password, nickname);
    }

    private String validatePassword(String password) {
        password = requireNotBlank(password);
        requireMinLength(password, PASSWORD_MIN_LENGTH);
        requireMatches(password, PASSWORD_PATTERN);

        return password;
    }
}
