package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserIdAndWithdrawal_WithdrawnAtIsNull(Long userId);

    Optional<UserEntity> findByEmailAndWithdrawal_WithdrawnAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByUserDetail_Nickname(String nickname);

    boolean existsByUserDetail_NicknameAndUserIdNot(String nickname, Long userId);
}
