package net.akive.api.auth.adapter.login.persistence;

import net.akive.api.auth.adapter.login.persistence.entity.LegacyUserPasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface LegacyUserPasswordJpaRepository extends JpaRepository<LegacyUserPasswordEntity, Long> {

    Optional<LegacyUserPasswordEntity> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
}
