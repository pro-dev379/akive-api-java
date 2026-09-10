package net.akive.api.user.application.required;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUserIdAndNotWithdrawn(Long userId);

    Optional<User> findByEmailAndNotWithdrawn(Email email);

    boolean existsByEmail(Email email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameNotAndUserIdNot(String nickname, Long userId);
}
