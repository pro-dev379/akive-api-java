package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.exception.UserException;
import org.springframework.stereotype.Component;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.exception.UserErrorCode.EMAIL_ALREADY_EXISTS;

@Component
@RequiredArgsConstructor
class SignUpValidator {

    private final ValidateUserPort validateUserPort;
    private final EnsureEmailVerifiedUseCase ensureEmailVerifiedUseCase;

    void validateEmailSignUpAvailable(Email email) {
        if (validateUserPort.existsByEmail(email)) {
            throw new UserException(EMAIL_ALREADY_EXISTS);
        }

        ensureEmailVerifiedUseCase.ensureVerified(
                new EnsureEmailVerifiedCommand(email, SIGN_UP)
        );
    }
}
