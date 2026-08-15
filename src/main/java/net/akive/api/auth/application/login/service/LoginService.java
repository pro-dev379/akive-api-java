package net.akive.api.auth.application.login.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.login.provided.LoginByEmailUseCase;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.provided.dto.LoginResult;
import net.akive.api.auth.application.login.required.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.login.required.LoadEmailLoginUserPort;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.user.application.provided.UpdatePasswordUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class LoginService implements LoginByEmailUseCase {

    private final TokenIssuer tokenIssuer;
    private final UpdatePasswordUseCase updatePasswordUseCase;

    private final LoadEmailLoginUserPort loadEmailLoginUserPort;
    private final DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    private final LoginDomainService loginDomainService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResult login(LoginByEmailCommand command) {
        LoginUser user = loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email());
        LoginDomainService.LoginAuthentication loginAuthentication =
                loginDomainService.authenticate(user, command.password());

        if (loginAuthentication.legacyPasswordUsed()) {
            migratePassword(user, command.password());
        }

        return issueToken(loginAuthentication.user(), command.clientInstanceId());
    }

    private void migratePassword(LoginUser user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        updatePasswordUseCase.updatePassword(user.userId(), encodedPassword);
        deleteLegacyUserPasswordPort.deleteByUserId(user.userId());
    }

    private LoginResult issueToken(LoginUser user, ClientInstanceId clientInstanceId) {
        TokenIssueCommand command = new TokenIssueCommand(user.userId(), clientInstanceId, user.roleType());

        IssuedToken token = tokenIssuer.issue(command);

        return new LoginResult(
                token.accessToken().value(),
                token.refreshToken().value()
        );
    }
}
