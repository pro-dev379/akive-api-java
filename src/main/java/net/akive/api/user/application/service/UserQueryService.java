package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserQueryService implements UserFinder, UserNicknameChecker {

    private final UserRepository userRepository;

    @Override
    public Optional<User> find(Long userId) {
        return userRepository.findByUserIdAndNotWithdrawn(userId);
    }

    @Override
    public User findOrThrow(Long userId) throws ResourceNotFoundException {
        return userRepository.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userRepository.findByEmailAndNotWithdrawn(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(requireMaxLength(requireNotBlank(nickname), NICKNAME_MAX_LENGTH));
    }

    @Override
    public boolean existsByNicknameExcludingUser(String nickname, Long userId) {
        return userRepository.existsByNicknameNotAndUserIdNot(
                requireMaxLength(requireNotBlank(nickname), NICKNAME_MAX_LENGTH),
                requirePositive(userId)
        );
    }
}
