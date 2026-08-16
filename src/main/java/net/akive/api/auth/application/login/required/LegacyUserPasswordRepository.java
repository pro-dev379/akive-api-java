package net.akive.api.auth.application.login.required;

import net.akive.api.auth.domain.login.LegacyUserPassword;

import java.util.Optional;

public interface LegacyUserPasswordRepository {

    Optional<LegacyUserPassword> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
