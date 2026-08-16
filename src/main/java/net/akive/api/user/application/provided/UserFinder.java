package net.akive.api.user.application.provided;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.User;

import java.util.Optional;

public interface UserFinder {

    Optional<User> findByUserIdAndLeaveFalse(Long userId);

    Optional<User> findByEmailAndLeaveFalse(Email email);
}
