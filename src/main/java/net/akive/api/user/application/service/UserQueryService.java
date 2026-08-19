package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService implements UserFinder {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUserIdAndLeaveFalse(Long userId) {
        return userRepository.findByUserIdAndLeaveFalse(userId);
    }

    @Override
    public Optional<User> findByEmailAndLeaveFalse(Email email) {
        return userRepository.findByEmailAndLeaveFalse(email);
    }
}
