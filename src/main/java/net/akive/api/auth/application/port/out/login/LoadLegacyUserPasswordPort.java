package net.akive.api.auth.application.port.out.login;

import net.akive.api.auth.domain.login.LegacyUserPassword;

import java.util.Optional;

public interface LoadLegacyUserPasswordPort {

    Optional<LegacyUserPassword> loadByUserId(Long userId);
}
