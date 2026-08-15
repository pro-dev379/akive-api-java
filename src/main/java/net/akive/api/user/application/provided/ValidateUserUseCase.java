package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;

public interface ValidateUserUseCase {

    void validateUserExistsByEmail(Email email) throws NotFoundException;

    void validateUserExistsById(Long userId) throws NotFoundException;

    void validateEmailNotDuplicate(Email email) throws ConflictException;
}
