package net.akive.api.auth.infrastructure.login;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.infrastructure.entity.LegacyUserPasswordEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class LegacyUserPasswordJpaAdapter implements
        LoadLegacyUserPasswordPort,
        DeleteLegacyUserPasswordPort {

    private final LegacyUserPasswordJpaRepository legacyUserPasswordJpaRepository;

    @Override
    public Optional<LegacyUserPassword> loadByUserId(Long userId) {
        return legacyUserPasswordJpaRepository
                .findByUser_UserId(userId)
                .map(LegacyUserPasswordEntity::to);
    }

    @Override
    public void deleteByUserId(Long userId) {
        legacyUserPasswordJpaRepository.deleteByUser_UserId(userId);
    }
}
