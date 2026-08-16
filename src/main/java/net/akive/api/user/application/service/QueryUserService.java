package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.required.LoadUserPort;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryUserService implements UserFinder {

    private final LoadUserPort loadUserPort;

    @Override
    public Optional<User> findByUserIdAndLeaveFalse(Long userId) {
        return loadUserPort.loadByUserIdAndLeaveFalse(userId);
    }

    @Override
    public Optional<User> findByEmailAndLeaveFalse(Email email) {
        return loadUserPort.findByEmailAndLeaveFalse(email);
    }
}
