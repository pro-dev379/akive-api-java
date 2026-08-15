package net.akive.api.auth.application.login.service;

import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.required.DeleteLegacyUserPasswordPort;
import net.akive.api.auth.application.login.required.LoadEmailLoginUserPort;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.user.application.provided.UpdatePasswordUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    TokenIssuer tokenIssuer;

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

//    @Test
//    void 로그인_인증에_성공하면_토큰을_발급한다() {
//        // given
//        LoginByEmailCommand command = new LoginByEmailCommand(new Email("test@test.com"), "password123!");
//        LoginUser user = loginUser("encoded-password");
//        Token token = new Token("access-token", "refresh-token");
//
//        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
//                .willReturn(user);
//        given(loginDomainService.authenticate(user, command.password()))
//                .willReturn(new LoginDomainService.LoginAuthentication(user, false));
//        given(tokenIssuer.issue(user.userId(), user.roleType()))
//                .willReturn(token);
//
//        // when
//        LoginResult result = loginService.login(command);
//
//        // then
//        assertThat(result.accessToken()).isEqualTo("access-token");
//        assertThat(result.rotatedRefreshToken()).isEqualTo("refresh-token");
//
//        then(loadEmailLoginUserPort).should().loadByEmailAndLeaveFalse(command.email());
//        then(loginDomainService).should().authenticate(user, command.password());
//        then(tokenIssuer).should().issue(user.userId(), user.roleType());
//        then(updatePasswordUseCase).shouldHaveNoInteractions();
//        then(deleteLegacyUserPasswordPort).shouldHaveNoInteractions();
//    }
//
//    @Test
//    void 레거시_패스워드로_인증되면_패스워드를_마이그레이션하고_토큰을_발급한다() {
//        // given
//        LoginByEmailCommand command = new LoginByEmailCommand(new Email("test@test.com"), "password123!");
//        LoginUser user = loginUser(null);
//        Token token = new Token("access-token", "refresh-token");
//
//        given(loadEmailLoginUserPort.loadByEmailAndLeaveFalse(command.email()))
//                .willReturn(user);
//        given(loginDomainService.authenticate(user, command.password()))
//                .willReturn(new LoginDomainService.LoginAuthentication(user, true));
//        given(passwordEncoder.encode(command.password()))
//                .willReturn("encoded-password");
//        given(tokenIssuer.issue(user.userId(), user.roleType()))
//                .willReturn(token);
//
//        // when
//        LoginResult result = loginService.login(command);
//
//        // then
//        assertThat(result.accessToken()).isEqualTo("access-token");
//        assertThat(result.rotatedRefreshToken()).isEqualTo("refresh-token");
//
//        then(updatePasswordUseCase).should().updatePassword(user.userId(), "encoded-password");
//        then(deleteLegacyUserPasswordPort).should().deleteByUserId(user.userId());
//        then(tokenIssuer).should().issue(user.userId(), user.roleType());
//    }

    @Test
    void 존재하지_않는_이메일이라면_로그인할_수_없다() {
        // given
        LoginByEmailCommand command = LoginByEmailCommand.of(
                UUID.randomUUID(),
                "not-found@test.com",
                "password123!"
        );

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
        then(tokenIssuer).shouldHaveNoInteractions();
    }

    @Test
    void 로그인_인증에_실패하면_토큰을_발급하지_않는다() {
        // given
        LoginByEmailCommand command = LoginByEmailCommand.of(
                UUID.randomUUID(),
                "test@test.com",
                "wrong-password"
        );
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
        then(tokenIssuer).shouldHaveNoInteractions();
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
