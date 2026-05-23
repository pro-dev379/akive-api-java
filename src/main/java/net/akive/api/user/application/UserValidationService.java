package net.akive.api.user.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.BusinessException;
import net.akive.api.user.application.port.out.UserRepository;
import net.akive.api.user.excpetion.EmailAlreadyExistsException;
import net.akive.api.user.excpetion.UserErrorCode;
import net.akive.api.user.excpetion.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserValidationService {

    private final UserRepository userRepository;

    public void validateUserExistsByEmail(String email) {
        if (!userRepository.existsByEmailAndLeaveFalse(email)) {
            throw new UserNotFoundException();
        }
    }

    public void validateUserExistsById(Long userId) {
        if (!userRepository.existsByUserIdAndLeaveFalse(userId)) {
            throw new EmailAlreadyExistsException();
        }
    }

    public void validateEmailNotDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }
}
