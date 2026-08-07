package net.akive.api.auth.application.login.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.login.required.LoadLegacyUserPasswordPort;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.common.exception.business.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;

@Component
@RequiredArgsConstructor
class LoginPasswordValidator {

    private final LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;
    private final PasswordEncoder passwordEncoder;
    private final LegacyPasswordVerifier legacyPasswordVerifier;

    boolean validateAndCheckLegacyPasswordUsed(LoginUser user, String rawPassword) {
        if (user.hasPassword()) {
            validatePassword(rawPassword, user.passwordValue());
            return false;
        }

        validateLegacyPassword(rawPassword, user);
        return true;
    }

    private void validatePassword(String rawPassword, String userPassword) {
        if (!passwordEncoder.matches(rawPassword, userPassword)) {
            throw new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void validateLegacyPassword(String rawPassword, LoginUser user) {
        LegacyUserPassword legacyUserPassword = loadLegacyUserPasswordPort
                .loadByUserId(user.userId())
                .orElseThrow(() -> new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        if (!legacyPasswordVerifier.matches(rawPassword, legacyUserPassword)) {
            throw new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD);
        }
    }
}
