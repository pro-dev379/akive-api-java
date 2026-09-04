package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

public interface UserValidator {

    void validateUserExistsByEmail(Email email) throws ResourceNotFoundException;

    void validateEmailNotDuplicate(Email email) throws ResourceConflictException;

    void validateNicknameNotDuplicate(String nickname) throws ResourceConflictException;
}
