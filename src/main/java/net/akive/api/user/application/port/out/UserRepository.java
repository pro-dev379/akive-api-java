package net.akive.api.user.application.port.out;

import net.akive.api.user.domain.User;

public interface UserRepository {

    User save(User user);
    boolean existsByUserIdAndLeaveFalse(Long userId);
    boolean existsByEmailAndLeaveFalse(String email);
    boolean existsByEmail(String email);
}
