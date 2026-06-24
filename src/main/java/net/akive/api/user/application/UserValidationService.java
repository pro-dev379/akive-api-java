package net.akive.api.user.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.excpetion.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.user.excpetion.UserErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserValidationService implements ValidateUserUseCase {

    private final ValidateUserPort validateUserPort;

    @Override
    public void validateUserExistsByEmail(String email) {
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
    public void validateEmailNotDuplicate(String email) {
        if (validateUserPort.existsByEmail(email)) {
            throw new UserException(EMAIL_ALREADY_EXISTS);
        }
    }
}
