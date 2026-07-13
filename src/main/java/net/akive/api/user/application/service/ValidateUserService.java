package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.exception.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.user.exception.UserErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ValidateUserService implements ValidateUserUseCase {

    private final ValidateUserPort validateUserPort;

    @Override
    public void validateUserExistsByEmail(Email email) {
        if (!validateUserPort.existsByEmailAndLeaveFalse(email)) {
            throw new UserException(NOT_FOUND);
        }
    }

    @Override
    public void validateUserExistsById(Long userId) {
        if (!validateUserPort.existsByUserIdAndLeaveFalse(userId)) {
            throw new UserException(NOT_FOUND);
        }
    }

    @Override
    public void validateEmailNotDuplicate(Email email) {
        if (validateUserPort.existsByEmail(email)) {
            throw new UserException(EMAIL_ALREADY_EXISTS);
        }
    }
}
