package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;
import net.akive.api.auth.application.dto.result.login.LoginResult;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.login.LoginByEmailUseCase;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class LoginService implements LoginByEmailUseCase {

    private final IssueTokenUseCase issueTokenUseCase;
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

        return issueToken(loginAuthentication.user());
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
