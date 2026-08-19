package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUserIdAndLeaveFalse(Long userId);

    Optional<User> findByEmailAndLeaveFalse(Email email);

    Optional<User> findByEmail(Email email);

    Optional<User> findByNickname(String nickname);

    User save(User user);
}
