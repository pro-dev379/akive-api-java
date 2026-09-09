package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserIdAndWithdrawal_WithdrawnAtIsNull(Long userId);

    Optional<UserEntity> findByEmailAndWithdrawal_WithdrawnAtIsNull(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUserDetail_Nickname(String nickname);
}
