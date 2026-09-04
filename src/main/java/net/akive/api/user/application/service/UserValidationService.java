package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.required.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserValidationService implements UserValidator {

    private final UserRepository userRepository;

    @Override
    public void validateUserExistsByEmail(Email email) throws ResourceNotFoundException {
        userRepository.findByEmailAndLeaveFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    @Override
    public void validateEmailNotDuplicate(Email email) throws ResourceConflictException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceConflictException(EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    public void validateNicknameNotDuplicate(String nickname) throws ResourceConflictException {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ResourceConflictException(NICKNAME_ALREADY_EXISTS);
        }
    }
}
