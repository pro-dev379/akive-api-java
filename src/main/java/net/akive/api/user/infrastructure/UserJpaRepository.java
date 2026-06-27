package net.akive.api.user.infrastructure;

import net.akive.api.user.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserIdAndLeaveFalse(Long userId);
    Optional<UserEntity> findByEmailAndLeaveFalse(String email);
    boolean existsByUserIdAndLeaveFalse(Long userId);
    boolean existsByEmailAndLeaveFalse(String email);
    boolean existsByEmail(String email);
}
