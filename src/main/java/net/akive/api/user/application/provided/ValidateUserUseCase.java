package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;

public interface ValidateUserUseCase {

    void validateUserExistsByEmail(Email email);
    void validateUserExistsById(Long userId);
    void validateEmailNotDuplicate(Email email);
}
