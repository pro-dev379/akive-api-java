package net.akive.api.auth.application.login.service;

import net.akive.api.common.exception.business.UnauthorizedException;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.domain.login.LoginUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;
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
    LoginPasswordValidator loginPasswordValidator;

    @InjectMocks
    LoginDomainService loginDomainService;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 로그인_패스워드_검증에_성공하면_레거시_패스워드_사용_여부를_반환한다(boolean legacyPasswordUsed) {
        // given
        LoginUser user = loginUser("encoded-password");

        given(loginPasswordValidator.validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD))
                .willReturn(legacyPasswordUsed);

        // when
        LoginDomainService.LoginAuthentication loginAuthentication = loginDomainService.authenticate(user, RAW_PASSWORD);

        // then
        assertThat(loginAuthentication.user()).isEqualTo(user);
        assertThat(loginAuthentication.legacyPasswordUsed()).isEqualTo(legacyPasswordUsed);
        then(loginPasswordValidator).should().validateAndCheckLegacyPasswordUsed(user, RAW_PASSWORD);
    }

    @Test
    void 로그인_패스워드_검증에_실패하면_예외를_던진다() {
        // given
        LoginUser user = loginUser("encoded-password");

        given(loginPasswordValidator.validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD))
                .willThrow(new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        // when & then
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> loginDomainService.authenticate(user, WRONG_PASSWORD))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);

        then(loginPasswordValidator).should().validateAndCheckLegacyPasswordUsed(user, WRONG_PASSWORD);
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
