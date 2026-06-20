package net.akive.api.user.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import net.akive.api.user.application.port.out.UserRepository;
import net.akive.api.user.excpetion.UserException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.user.excpetion.UserErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserValidationService implements ValidateUserUseCase {

    private final UserRepository userRepository;

    @Override
    public void validateUserExistsByEmail(String email) {
        if (!userRepository.existsByEmailAndLeaveFalse(email)) {
            throw new UserException(NOT_FOUND);
        }
    }

    @Override
    public void validateUserExistsById(Long userId) {
        if (!userRepository.existsByUserIdAndLeaveFalse(userId)) {
            throw new UserException(NOT_FOUND);
        }
    }

    @Override
    public void validateEmailNotDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(EMAIL_ALREADY_EXISTS);
        }
    }
}
