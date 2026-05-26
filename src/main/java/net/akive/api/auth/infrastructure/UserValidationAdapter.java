package net.akive.api.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.EmailVerificationUserValidator;
import net.akive.api.user.application.UserValidationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidationAdapter implements EmailVerificationUserValidator {

    private final UserValidationService userValidationService;

    @Override
    public void validateUserExistsByEmail(String email) {
        userValidationService.validateUserExistsByEmail(email);
    }

    @Override
    public void validateEmailNotDuplicate(String email) {
        userValidationService.validateEmailNotDuplicate(email);
    }
}
