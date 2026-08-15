package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.ValidateUserUseCase;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class EmailVerificationValidationService {

    private final ValidateUserUseCase validateUserUseCase;

    public void validateIssuable(Email email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case FIND_PASSWORD -> validateUserUseCase.validateUserExistsByEmail(email);
            case SIGN_UP -> validateUserUseCase.validateEmailNotDuplicate(email);
        }
    }
}
