package net.akive.api.auth.application.port.out;

public interface EmailVerificationUserValidator {

    void validateUserExistsByEmail(String email);
    void validateEmailNotDuplicate(String email);
}
