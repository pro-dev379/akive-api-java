package net.akive.api.auth.application;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.application.port.out.login.LoadLegacyUserPasswordPort;
import net.akive.api.auth.domain.login.LegacyUserPassword;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
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
class LoginPasswordPolicyTest {

    private static final String RAW_PASSWORD = "password123!";
    private static final String WRONG_PASSWORD = "wrong-password";

    @Mock
    LoadLegacyUserPasswordPort loadLegacyUserPasswordPort;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    LegacyPasswordVerifier legacyPasswordVerifier;

    @InjectMocks
    LoginPasswordPolicy loginPasswordPolicy;

    @Test
    void 신규_패스워드와_일치하면_레거시_패스워드_사용_여부를_false로_반환한다() {
        // given
        LoginUser user = loginUser("encoded-password");
        given(passwordEncoder.matches(RAW_PASSWORD, user.passwordValue()))
                .willReturn(true);

        // when
        boolean legacyPasswordUsed = loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD);

        // then
        assertThat(legacyPasswordUsed).isFalse();
        then(loadLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(legacyPasswordVerifier).shouldHaveNoInteractions();
    }

    @Test
    void 신규_패스워드와_일치하지_않으면_로그인할_수_없다() {
        // given
        LoginUser user = loginUser("encoded-password");
        given(passwordEncoder.matches(WRONG_PASSWORD, user.passwordValue()))
                .willReturn(false);

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(loadLegacyUserPasswordPort).shouldHaveNoInteractions();
        then(legacyPasswordVerifier).shouldHaveNoInteractions();
    }

    @Test
    void 신규_패스워드가_없고_레거시_패스워드와_일치하면_레거시_패스워드_사용_여부를_true로_반환한다() {
        // given
        LoginUser user = loginUser(null);
        LegacyUserPassword legacyUserPassword = legacyUserPassword();

        given(loadLegacyUserPasswordPort.loadByUserId(user.userId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(RAW_PASSWORD, legacyUserPassword))
                .willReturn(true);

        // when
        boolean legacyPasswordUsed = loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD);

        // then
        assertThat(legacyPasswordUsed).isTrue();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 신규_패스워드가_없고_레거시_패스워드도_없으면_로그인할_수_없다() {
        // given
        LoginUser user = loginUser(null);
        given(loadLegacyUserPasswordPort.loadByUserId(user.userId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(legacyPasswordVerifier).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 신규_패스워드가_없고_레거시_패스워드와_일치하지_않으면_로그인할_수_없다() {
        // given
        LoginUser user = loginUser(null);
        LegacyUserPassword legacyUserPassword = legacyUserPassword();

        given(loadLegacyUserPasswordPort.loadByUserId(user.userId()))
                .willReturn(Optional.of(legacyUserPassword));
        given(legacyPasswordVerifier.matches(WRONG_PASSWORD, legacyUserPassword))
                .willReturn(false);

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(passwordEncoder).shouldHaveNoInteractions();
    }

    private LoginUser loginUser(String password) {
        return LoginUser.create(
                1L,
                Email.of("test@test.com"),
                password,
                USER
        );
    }

    private LegacyUserPassword legacyUserPassword() {
        return new LegacyUserPassword(
                1L,
                "legacy-password",
                "legacy-salt"
        );
    }
}
