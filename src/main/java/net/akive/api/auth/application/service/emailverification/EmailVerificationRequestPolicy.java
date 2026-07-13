package net.akive.api.auth.application.service.emailverification;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class EmailVerificationRequestPolicy {

    private final ValidateUserUseCase validateUserUseCase;

    void validateRequestAvailable(Email email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case FIND_PASSWORD -> validateUserUseCase.validateUserExistsByEmail(email);
            case SIGN_UP -> validateUserUseCase.validateEmailNotDuplicate(email);
        }
    }
}
