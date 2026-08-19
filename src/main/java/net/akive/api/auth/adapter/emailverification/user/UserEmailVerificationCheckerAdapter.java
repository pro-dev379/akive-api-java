package net.akive.api.auth.adapter.emailverification.user;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationChecker;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.user.application.required.UserEmailVerificationChecker;
import org.springframework.stereotype.Component;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;

@Component
@RequiredArgsConstructor
class UserEmailVerificationCheckerAdapter implements UserEmailVerificationChecker {

    private final EmailVerificationChecker emailVerificationChecker;

    @Override
    public void ensureEmailVerifiedForSignUp(Email email) throws BadRequestException {
        emailVerificationChecker.ensureEmailVerified(new EmailVerificationEnsureCommand(email, SIGN_UP));
    }

    @Override
    public void ensureEmailVerifiedForPasswordReset(Email email) throws BadRequestException {
        emailVerificationChecker.ensureEmailVerified(new EmailVerificationEnsureCommand(email, PASSWORD_RESET));
    }
}
