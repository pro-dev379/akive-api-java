package net.akive.api.auth.application;

import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;
import net.akive.api.auth.application.dto.result.LoginResult;
import net.akive.api.auth.application.dto.result.TokenIssueResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    IssueTokenUseCase issueTokenUseCase;

    @Mock
    UpdatePasswordUseCase updatePasswordUseCase;

    @Mock
    LoadEmailLoginUserPort loadEmailLoginUserPort;

    @Mock
    LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;

    @Mock
    DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    LegacyPasswordVerifier legacyPasswordVerifier;

    @InjectMocks
    LoginService loginService;

    @Test
    void 저장된_패스워드와_일치하면_토큰을_발급한다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("test@test.com", "password123!");

        LoginUser user = new LoginUser(
                1L,
                "test@test.com",
                "encoded-password",
                USER
        );

        TokenIssueResult token = new TokenIssueResult("access-token", "refresh-token");

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(passwordEncoder.matches(command.password(), user.password()))
                .willReturn(true);
        given(issueTokenUseCase.issue(user.userId(), user.roleType()))
                .willReturn(token);

        // when
        LoginResult result = loginService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        then(loadLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(legacyPasswordVerifier).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
    }

    @Test
    void 존재하지_않는_이메일이라면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("not-found@test.com", "password123!");

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willThrow(new LoginException(INVALID_EMAIL_OR_PASSWORD));

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(passwordEncoder).shouldHaveNoInteractions();
        then(loadLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(legacyPasswordVerifier).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 저장된_패스워드와_일치하지_않으면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("test@test.com", "wrong-password");

        LoginUser user = new LoginUser(
                1L,
                "test@test.com",
                "encoded-password",
                USER
        );

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(passwordEncoder.matches(command.password(), user.password()))
                .willReturn(false);

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(loadLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(legacyPasswordVerifier).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 저장된_레거시_패스워드와_일치하면_패스워드를_마이그레이션하고_토큰을_발급한다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("test@test.com", "password123!");

        LoginUser user = new LoginUser(
                1L,
                "test@test.com",
                null,
                USER
        );

        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(
                1L,
                "legacy-password",
                "legacy-salt"
        );

        TokenIssueResult token = new TokenIssueResult("access-token", "refresh-token");

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loadLegacyUserPasswordPort.findByUserId(user.userId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(command.password(), legacyUserPassword))
                .willReturn(true);
        given(passwordEncoder.encode(command.password()))
                .willReturn("encoded-password");
        given(issueTokenUseCase.issue(user.userId(), user.roleType()))
                .willReturn(token);

        // when
        LoginResult result = loginService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        then(updatePasswordUseCase).should()
                .updatePassword(user.userId(), "encoded-password");
        then(deleteLegacyUserPasswordPort).should()
                .deleteByUserId(user.userId());
    }

    @Test
    void 저장된_레거시_패스워드가_없으면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("test@test.com", "password123!");

        LoginUser user = new LoginUser(
                1L,
                "test@test.com",
                null,
                USER
        );

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loadLegacyUserPasswordPort.findByUserId(user.userId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(legacyPasswordVerifier).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 저장된_레거시_패스워드와_일치하지_않으면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand("test@test.com", "wrong-password");

        LoginUser user = new LoginUser(
                1L,
                "test@test.com",
                null,
                USER
        );

        LegacyUserPassword legacyUserPassword = new LegacyUserPassword(
                1L,
                "legacy-password",
                "legacy-salt"
        );

        given(loadEmailLoginUserPort.findByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loadLegacyUserPasswordPort.findByUserId(user.userId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(command.password(), legacyUserPassword))
                .willReturn(false);

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }
}