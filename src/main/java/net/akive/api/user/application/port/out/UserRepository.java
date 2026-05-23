package net.akive.api.user.application.port.out;

public interface UserRepository {

    boolean existsByUserIdAndLeaveFalse(Long userId);
    boolean existsByEmailAndLeaveFalse(String email);
    boolean existsByEmail(String email);
}
