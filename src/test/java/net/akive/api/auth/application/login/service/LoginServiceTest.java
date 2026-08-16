package net.akive.api.auth.application.login.service;

import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.application.login.required.LegacyUserPasswordRepository;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.user.application.provided.UpdatePasswordUseCase;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.RAW_PASSWORD;
import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.createLegacyUserPassword;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final Long USER_ID = 1L;
    private static final String PASSWORD_HASH = "password-hash";
    private static final String MIGRATED_PASSWORD_HASH = "migrated-password-hash";

    @Mock
    UserFinder userFinder;

    @Mock
    UpdatePasswordUseCase updatePasswordUseCase;

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

        given(userFinder.findByEmailAndLeaveFalse(command.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.password(), user.getPasswordHash())).willReturn(true);
        given(tokenIssuer.issue(tokenIssueCommand)).willReturn(issuedToken);

        IssuedToken result = loginService.loginByEmail(command);

        assertThat(result).isSameAs(issuedToken);

        then(userFinder).should().findByEmailAndLeaveFalse(command.email());
        then(passwordEncoder).should().matches(command.password(), user.getPasswordHash());
        then(tokenIssuer).should().issue(tokenIssueCommand);
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 레거시_패스워드로_이메일_로그인_하면_패스워드를_마이그레이션_하고_토큰을_발급한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUserWithoutPasswordHash(USER_ID);
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(USER_ID);
        IssuedToken issuedToken = createIssuedToken();
        TokenIssueCommand tokenIssueCommand = createTokenIssueCommand(command, user);

        given(userFinder.findByEmailAndLeaveFalse(command.email())).willReturn(Optional.of(user));
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
        then(updatePasswordUseCase).should().updatePassword(user.getUserId(), MIGRATED_PASSWORD_HASH);
        then(legacyUserPasswordRepository).should().deleteByUserId(user.getUserId());
        then(tokenIssuer).should().issue(tokenIssueCommand);
    }

    @Test
    void 이메일이_존재하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();

        given(userFinder.findByEmailAndLeaveFalse(command.email())).willReturn(Optional.empty());

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 패스워드가_일치하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUser(USER_ID, PASSWORD_HASH);

        given(userFinder.findByEmailAndLeaveFalse(command.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(command.password(), user.getPasswordHash())).willReturn(false);

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).shouldHaveNoInteractions();
        then(updatePasswordUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 레거시_패스워드가_일치하지_않으면_이메일_로그인에_실패한다() {
        LoginByEmailCommand command = createLoginByEmailCommand();
        User user = createUserWithoutPasswordHash(USER_ID);
        LegacyUserPassword legacyUserPassword = createLegacyUserPassword(USER_ID);

        given(userFinder.findByEmailAndLeaveFalse(command.email())).willReturn(Optional.of(user));
        given(legacyUserPasswordRepository.findByUserId(user.getUserId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(command.password(), legacyUserPassword.passwordHash()))
                .willReturn(false);

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginService.loginByEmail(command))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(tokenIssuer).shouldHaveNoInteractions();
        then(passwordEncoder).should(never()).encode(any());
        then(updatePasswordUseCase).shouldHaveNoInteractions();
        then(legacyUserPasswordRepository).should(never()).deleteByUserId(any());
    }

    private LoginByEmailCommand createLoginByEmailCommand() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        Email email = createEmail();

        return new LoginByEmailCommand(clientInstanceId, email, RAW_PASSWORD);
    }

    private User createUserWithoutPasswordHash(Long userId) {
        return createUser(userId, null);
    }

    private TokenIssueCommand createTokenIssueCommand(LoginByEmailCommand command, User user) {
        return new TokenIssueCommand(user.getUserId(), command.clientInstanceId(), user.getRoleType());
    }
}
