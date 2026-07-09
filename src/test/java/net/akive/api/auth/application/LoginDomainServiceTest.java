package net.akive.api.auth.application;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LoginDomainServiceTest {

    private static final String RAW_PASSWORD = "password123!";
    private static final String WRONG_PASSWORD = "wrong-password";

    @Mock
    LoginPasswordPolicy loginPasswordPolicy;

    @InjectMocks
    LoginDomainService loginDomainService;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 로그인_패스워드_검증에_성공하면_레거시_패스워드_사용_여부를_반환한다(boolean legacyPasswordUsed) {
        // given
        LoginUser user = loginUser("encoded-password");

        given(loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD))
                .willReturn(legacyPasswordUsed);

        // when
        LoginDomainService.LoginAuthentication loginAuthentication =
                loginDomainService.authenticate(user, RAW_PASSWORD);

        // then
        assertThat(loginAuthentication.user()).isEqualTo(user);
        assertThat(loginAuthentication.legacyPasswordUsed()).isEqualTo(legacyPasswordUsed);
        then(loginPasswordPolicy).should().validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD);
    }

    @Test
    void 로그인_패스워드_검증에_실패하면_예외를_던진다() {
        // given
        LoginUser user = loginUser("encoded-password");

        given(loginPasswordPolicy.validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD))
                .willThrow(new LoginException(INVALID_EMAIL_OR_PASSWORD));

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loginDomainService.authenticate(user, WRONG_PASSWORD))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(loginPasswordPolicy).should().validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD);
    }

    private LoginUser loginUser(String password) {
        return LoginUser.create(
                1L,
                Email.of("test@test.com"),
                password,
                USER
        );
    }
}
