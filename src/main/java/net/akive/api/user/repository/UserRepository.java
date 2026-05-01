package net.akive.api.user.repository;

import net.akive.api.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByUserIdAndLeaveFalse(Long userId);
    boolean existsByEmailAndLeaveFalse(String email);
    boolean existsByEmail(String email);
}
