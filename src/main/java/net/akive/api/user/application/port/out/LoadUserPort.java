package net.akive.api.user.application.port.out;

import net.akive.api.user.domain.User;

import java.util.Optional;

public interface LoadUserPort {

    Optional<User> findByUserIdAndLeaveFalse(Long userId);
    Optional<User> findByEmailAndLeaveFalse(String email);
}
