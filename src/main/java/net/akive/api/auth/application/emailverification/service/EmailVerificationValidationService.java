package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.UserValidator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class EmailVerificationValidationService {

    private final UserValidator userValidator;

    public void validateIssuable(Email email, EmailVerificationPurpose verificationPurpose) {
        switch (verificationPurpose) {
            case SIGN_UP -> userValidator.validateEmailNotDuplicate(email);
            case PASSWORD_RESET -> userValidator.validateUserExistsByEmail(email);
        }
    }
}
