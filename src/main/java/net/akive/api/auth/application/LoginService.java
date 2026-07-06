package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;
import net.akive.api.auth.application.dto.result.login.LoginResult;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.login.LoginByEmailUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;

@Service
@RequiredArgsConstructor
class LoginService implements LoginByEmailUseCase {

    private final IssueTokenUseCase issueTokenUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;

    private final LoadEmailLoginUserPort loadEmailLoginUserPort;
    private final LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;
    private final DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    private final PasswordEncoder passwordEncoder;
    private final LegacyPasswordVerifier legacyPasswordVerifier;

    @Override
    @Transactional
    public LoginResult login(LoginByEmailCommand command) {
        LoginUser user = loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email());

        if (hasPassword(user)) {
            validatePassword(command.password(), user.password());
            return issueToken(user);
        }

        validateLegacyPassword(command.password(), user);
        migratePassword(user, command.password());

        return issueToken(user);
    }

    private boolean hasPassword(LoginUser user) {
        return user.password() != null && !user.password().isBlank();
    }

    private void validatePassword(String rawPassword, String userPassword) {
        if (!passwordEncoder.matches(rawPassword, userPassword)) {
            throw new LoginException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void validateLegacyPassword(String rawPassword, LoginUser user) {
        LegacyUserPassword legacyUserPassword
                = loadLegacyUserPasswordPort
                .loadByUserId(user.userId())
                .orElseThrow(() -> new LoginException(INVALID_EMAIL_OR_PASSWORD));

        if (!legacyPasswordVerifier.matches(rawPassword, legacyUserPassword)) {
            throw new LoginException(INVALID_EMAIL_OR_PASSWORD);
        }
    }

    private void migratePassword(LoginUser user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        updatePasswordUseCase.updatePassword(user.userId(), encodedPassword);
        deleteLegacyUserPasswordPort.deleteByUserId(user.userId());
    }

    private LoginResult issueToken(LoginUser user) {
        IssueTokenResult token = issueTokenUseCase.issue(user.userId(), user.roleType());

        return new LoginResult(
                token.accessToken(),
                token.refreshToken()
        );
    }
}
