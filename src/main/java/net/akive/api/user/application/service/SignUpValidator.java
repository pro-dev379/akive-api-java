package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.provided.EmailVerifiedChecker;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerifiedCheckCommand;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.user.application.required.ValidateUserPort;
import org.springframework.stereotype.Component;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;

@Component
@RequiredArgsConstructor
class SignUpValidator {

    private final ValidateUserPort validateUserPort;
    private final EmailVerifiedChecker emailVerifiedChecker;

    void validateEmailSignUpAvailable(Email email) {
        if (validateUserPort.existsByEmail(email)) {
            throw new ConflictException(EMAIL_ALREADY_EXISTS);
        }

        emailVerifiedChecker.checkVerified(new EmailVerifiedCheckCommand(email, SIGN_UP));
    }
}
