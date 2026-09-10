package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserQueryService implements UserFinder {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUserIdAndNotWithdrawn(Long userId) {
        return userRepository.findByUserIdAndNotWithdrawn(userId);
    }

    @Override
    public User findOrThrowByUserIdAndNotWithdrawn(Long userId) throws ResourceNotFoundException {
        return userRepository.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    @Override
    public Optional<User> findByEmailAndNotWithdrawn(Email email) {
        return userRepository.findByEmailAndNotWithdrawn(email);
    }
}
