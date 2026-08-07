package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.ValidateUserUseCase;
import net.akive.api.user.application.required.ValidateUserPort;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ConflictError.*;
import static net.akive.api.common.exception.business.NotFoundError.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ValidateUserService implements ValidateUserUseCase {

    private final ValidateUserPort validateUserPort;

    @Override
    public void validateUserExistsByEmail(Email email) {
        if (!validateUserPort.existsByEmailAndLeaveFalse(email)) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
    }

    @Override
    public void validateUserExistsById(Long userId) {
        if (!validateUserPort.existsByUserIdAndLeaveFalse(userId)) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
    }

    @Override
    public void validateEmailNotDuplicate(Email email) {
        if (validateUserPort.existsByEmail(email)) {
            throw new ConflictException(EMAIL_ALREADY_EXISTS);
        }
    }
}
