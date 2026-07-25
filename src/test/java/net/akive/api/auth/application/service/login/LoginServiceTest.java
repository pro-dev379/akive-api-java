package net.akive.api.auth.application.service.login;

import net.akive.api.common.exception.business.UnauthorizedException;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;
import net.akive.api.auth.application.dto.result.login.LoginResult;
import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.login.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.port.out.login.LoadEmailLoginUserPort;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.user.application.port.in.UpdatePasswordUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    IssueTokenUseCase issueTokenUseCase;

    @Mock
    UpdatePasswordUseCase updatePasswordUseCase;

    @Mock
    LoadEmailLoginUserPort loadEmailLoginUserPort;

    @Mock
    DeleteLegacyUserPasswordPort deleteLegacyUserPasswordPort;

    @Mock
    LoginDomainService loginDomainService;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    LoginService loginService;

    @Test
    void 로그인_인증에_성공하면_토큰을_발급한다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand(new Email("test@test.com"), "password123!");
        LoginUser user = loginUser("encoded-password");
        IssueTokenResult token = new IssueTokenResult("access-token", "refresh-token");

        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loginDomainService.authenticate(user, command.password()))
                .willReturn(new LoginDomainService.LoginAuthentication(user, false));
        given(issueTokenUseCase.issue(user.userId(), user.roleType()))
                .willReturn(token);

        // when
        LoginResult result = loginService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        then(loadEmailLoginUserPort).should().loadByEmailAndLeaveFalse(command.email());
        then(loginDomainService).should().authenticate(user, command.password());
        then(issueTokenUseCase).should().issue(user.userId(), user.roleType());
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
    }

    @Test
    void 레거시_패스워드로_인증되면_패스워드를_마이그레이션하고_토큰을_발급한다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand(new Email("test@test.com"), "password123!");
        LoginUser user = loginUser(null);
        IssueTokenResult token = new IssueTokenResult("access-token", "refresh-token");

        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loginDomainService.authenticate(user, command.password()))
                .willReturn(new LoginDomainService.LoginAuthentication(user, true));
        given(passwordEncoder.encode(command.password()))
                .willReturn("encoded-password");
        given(issueTokenUseCase.issue(user.userId(), user.roleType()))
                .willReturn(token);

        // when
        LoginResult result = loginService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        then(updatePasswordUseCase).should().updatePassword(user.userId(), "encoded-password");
        then(deleteLegacyUserPasswordPort).should().deleteByUserId(user.userId());
        then(issueTokenUseCase).should().issue(user.userId(), user.roleType());
    }

    @Test
    void 존재하지_않는_이메일이라면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand(new Email("not-found@test.com"), "password123!");

        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
                .willThrow(new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        // when & then
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(loginDomainService).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 로그인_인증에_실패하면_토큰을_발급하지_않는다() {
        // given
        LoginByEmailCommand command = new LoginByEmailCommand(new Email("test@test.com"), "wrong-password");
        LoginUser user = loginUser("encoded-password");

        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
                .willReturn(user);
        given(loginDomainService.authenticate(user, command.password()))
                .willThrow(new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        // when & then
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginService.login(command))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(updatePasswordUseCase).should(never()).updatePassword(any(), any());
        then(deleteLegacyUserPasswordPort).should(never()).deleteByUserId(any());
        then(issueTokenUseCase).shouldHaveNoInteractions();
    }

    private LoginUser loginUser(String password) {
        return LoginUser.create(
                1L,
                new Email("test@test.com"),
                password,
                USER
        );
    }
}
