package net.akive.api.auth.email.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.port.out.EmailAuthUserValidator;
import net.akive.api.user.application.UserValidationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailAuthUserValidationAdapter implements EmailAuthUserValidator {

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
