package net.akive.api.user.application.provided.dto;

import net.akive.api.common.domain.DomainValidator;
import net.akive.api.common.domain.Email;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.*;

public record UserSignUpByEmailCommand(
        Email email,
        String password,
        String nickname,
        List<SignUpLegalDocumentActionCommand> legalDocumentActions
) {

    public UserSignUpByEmailCommand {
        requireNonNull(email);
        password = validatePassword(password);
        requireMaxLengthIfPresent(nickname, NICKNAME_MAX_LENGTH);
        requireNotEmpty(legalDocumentActions).forEach(DomainValidator::requireNonNull);
        legalDocumentActions = List.copyOf(legalDocumentActions);
    }

    public static UserSignUpByEmailCommand of(
            String email,
            String password,
            String nickname,
            List<SignUpLegalDocumentActionCommand> legalDocumentActions
    ) {
        return new UserSignUpByEmailCommand(new Email(email), password, nickname, legalDocumentActions);
    }

    private String validatePassword(String password) {
        password = requireNotBlank(password);
        requireMinLength(password, PASSWORD_MIN_LENGTH);
        requireMatches(password, PASSWORD_PATTERN);

        return password;
    }
}
