package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.user.application.provided.UserValidator;
import net.akive.api.user.application.required.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserValidationService implements UserValidator {

    private final UserRepository userRepository;

    @Override
    public void validateUserExistsByEmail(Email email) throws NotFoundException {
        userRepository.findByEmailAndLeaveFalse(email)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    @Override
    public void validateEmailNotDuplicate(Email email) throws ConflictException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException(EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    public void validateNicknameNotDuplicate(String nickname) throws ConflictException {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ConflictException(NICKNAME_ALREADY_EXISTS);
        }
    }
}
