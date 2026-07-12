package net.akive.api.auth.infrastructure.login;

import net.akive.api.auth.infrastructure.entity.LegacyUserPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegacyUserPasswordJpaRepository extends JpaRepository<LegacyUserPasswordEntity, Long> {

    Optional<LegacyUserPasswordEntity> findByUser_UserId(Long userId);
    void deleteByUser_UserId(Long userId);
}
