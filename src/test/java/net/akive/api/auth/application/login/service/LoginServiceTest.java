package net.akive.api.auth.application.login.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static net.akive.api.auth.application.login.fixture.LoginCommandFixture.createLoginByEmailCommand;
import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyUserPassword;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String MIGRATED_PASSWORD_HASH = "migrated-password-hash";

    @Mock
    UserFinder userFinder;

    @Mock
    UserAccountManager userAccountManager;

    @Mock
    TokenIssuer tokenIssuer;

    @Mock
    LegacyUserPasswordRepository legacyUserPasswordRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    LegacyPasswordVerifier legacyPasswordVerifier;

    @InjectMocks
    LoginService loginService;

    @Test
    void 이메일_로그인에_성공하면_토큰을_발급한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUser(USER_ID, PASSWORD_HASH);
        IssuedToken issuedToken = createIssuedToken();
        TokenIssueCommand tokenIssueCommand = createTokenIssueCommand(command, user);

        given(userFinder.findByEmail(command.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.password(), user.getPasswordHash())).willReturn(true);
        given(tokenIssuer.issue(tokenIssueCommand)).willReturn(issuedToken);

        IssuedToken result = loginService.loginByEmail(command);

        assertThat(result).isSameAs(issuedToken);

        then(userFinder).should().findByEmail(command.email());
        then(passwordEncoder).should().matches(command.password(), user.getPasswordHash());
        then(tokenIssuer).should().issue(tokenIssueCommand);
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 레거시_패스워드로_이메일_로그인_하면_패스워드를_마이그레이션_하고_토큰을_발급한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUser(USER_ID, null);
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(user);
        IssuedToken issuedToken = createIssuedToken();
        TokenIssueCommand tokenIssueCommand = createTokenIssueCommand(command, user);

        given(userFinder.findByEmail(command.email())).willReturn(Optional.of(user));
        given(legacyUserPasswordRepository.findByUserId(user.getUserId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(command.password(), legacyUserPassword.passwordHash()))
                .willReturn(true);
        given(passwordEncoder.encode(command.password())).willReturn(MIGRATED_PASSWORD_HASH);
        given(tokenIssuer.issue(tokenIssueCommand)).willReturn(issuedToken);

        IssuedToken result = loginService.loginByEmail(command);

        assertThat(result).isSameAs(issuedToken);

        then(legacyUserPasswordRepository).should().findByUserId(user.getUserId());
        then(legacyPasswordVerifier).should().matches(command.password(), legacyUserPassword.passwordHash());
        then(passwordEncoder).should().encode(command.password());
        then(userAccountManager).should().updatePasswordByUserId(user.getUserId(), MIGRATED_PASSWORD_HASH);
        then(legacyUserPasswordRepository).should().deleteByUserId(user.getUserId());
        then(tokenIssuer).should().issue(tokenIssueCommand);
    }

    @Test
    void 이메일이_존재하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();

        given(userFinder.findByEmail(command.email())).willReturn(Optional.empty());

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 패스워드가_일치하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUser(USER_ID, PASSWORD_HASH);

        given(userFinder.findByEmail(command.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.password(), user.getPasswordHash())).willReturn(false);

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(userAccountManager).shouldHaveNoInteractions();
    }

    @Test
    void 레거시_패스워드가_일치하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUser(USER_ID, null);
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(user);

        given(userFinder.findByEmail(command.email())).willReturn(Optional.of(user));
        given(legacyUserPasswordRepository.findByUserId(user.getUserId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(command.password(), legacyUserPassword.passwordHash()))
                .willReturn(false);

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(passwordEncoder).should(never()).encode(any());
        then(userAccountManager).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).should(never()).deleteByUserId(any());
    }

    private TokenIssueCommand createTokenIssueCommand(LoginByEmailCommand command, User user) {
        return new TokenIssueCommand(user.getUserId(), command.clientInstanceId(), user.getRoleType());
    }
}
