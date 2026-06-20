package net.akive.api.user.application.port.in;

public interface ValidateUserUseCase {

    void validateUserExistsByEmail(String email);
    void validateUserExistsById(Long userId);
    void validateEmailNotDuplicate(String email);
}
