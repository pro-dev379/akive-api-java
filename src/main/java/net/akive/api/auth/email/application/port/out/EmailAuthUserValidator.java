package net.akive.api.auth.email.application.port.out;

public interface EmailAuthUserValidator {

    void validateUserExistsByEmail(String email);
    void validateEmailNotDuplicate(String email);
}
