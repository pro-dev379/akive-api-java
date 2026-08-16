package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.User;

import java.util.Optional;

public interface LoadUserPort {

    Optional<User> loadByUserIdAndLeaveFalse(Long userId);

    Optional<User> findByEmailAndLeaveFalse(Email email);
}
