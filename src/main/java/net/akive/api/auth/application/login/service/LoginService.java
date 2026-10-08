package net.akive.api.auth.application.login.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.required.LegacyUserPasswordRepository;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;

@Service
@RequiredArgsConstructor
class LoginService implements LoginProcessor {

    private final UserFinder userFinder;
    private final UserAccountManager userAccountManager;
    private final TokenIssuer tokenIssuer;
    private final LegacyUserPasswordRepository legacyUserPasswordRepository;

    private final PasswordEncoder passwordEncoder;
    private final LegacyPasswordVerifier legacyPasswordVerifier;

    @Override
    @Transactional
    public IssuedToken loginByEmail(LoginByEmailCommand command) throws AuthenticationException {
        User user = userFinder.findByEmail(command.email())
                .orElseThrow(() -> new AuthenticationException(INVALID_EMAIL_OR_PASSWORD));

        if (authenticateAndCheckLegacyPasswordUsed(user, command.password())) {
            migratePassword(user, command.password());
        }

        return tokenIssuer.issue(new TokenIssueCommand(
                user.getUserId(),
                command.clientInstanceId(),
                user.getRole()
        ));
    }

    private boolean authenticateAndCheckLegacyPasswordUsed(User user, String rawPassword) {
        if (user.hasPassword()) {
            validatePassword(rawPassword, user.getPasswordHash());

            return false;
        }

        validateLegacyPassword(rawPassword, user);

        return true;
    }

    private void validatePassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new AuthenticationException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void validateLegacyPassword(String rawPassword, User user) {
        LegacyUserPassword legacyUserPassword = legacyUserPasswordRepository
                .findByUserId(user.getUserId())
                .orElseThrow(() -> new AuthenticationException(INVALID_EMAIL_OR_PASSWORD));

        if (!legacyPasswordVerifier.matches(rawPassword, legacyUserPassword.passwordHash())) {
            throw new AuthenticationException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void migratePassword(User user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        userAccountManager.updatePasswordByUserId(user.getUserId(), encodedPassword);

        legacyUserPasswordRepository.deleteByUserId(user.getUserId());
    }
}
