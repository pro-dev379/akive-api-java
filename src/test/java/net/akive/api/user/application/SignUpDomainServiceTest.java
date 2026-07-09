package net.akive.api.user.application;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.domain.User;
import net.akive.api.user.exception.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_VERIFIED;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
import static net.akive.api.user.exception.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SignUpDomainServiceTest {

    private static final Email VALID_EMAIL = Email.of("test@test.com");
    private static final String VALID_PASSWORD = "password123!";

    @Mock
    SignUpPolicy signUpPolicy;

    @Mock
    GenerateNicknamePort generateNicknamePort;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    SignUpDomainService signUpDomainService;

    @Test
    void 이메일_회원가입_가능_검증_후_회원을_생성한다() {
        // given
        given(generateNicknamePort.generate(USER)).willReturn("USER@abcde");
        given(passwordEncoder.encode(VALID_PASSWORD)).willReturn("encoded-password");

        // when
        User user = signUpDomainService.signUpByEmail(VALID_EMAIL, VALID_PASSWORD, USER);

        // then
        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getLoginType()).isEqualTo(EMAIL);
        assertThat(user.getNickname()).isEqualTo("USER@abcde");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRoleType()).isEqualTo(USER);

        then(signUpPolicy).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).should().generate(USER);
        then(passwordEncoder).should().encode(VALID_PASSWORD);
    }

    @Test
    void 이미_가입된_이메일이면_회원_생성에_실패한다() {
        // given
        willThrow(new UserException(EMAIL_ALREADY_EXISTS))
                .given(signUpPolicy)
                .validateEmailSignUpAvailable(VALID_EMAIL);

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> signUpDomainService.signUpByEmail(VALID_EMAIL, VALID_PASSWORD, USER))
                .extracting(UserException::getErrorCode)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(signUpPolicy).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_인증이_완료되지_않으면_회원_생성에_실패한다() {
        // given
        willThrow(new EmailVerificationException(NOT_VERIFIED))
                .given(signUpPolicy)
                .validateEmailSignUpAvailable(VALID_EMAIL);

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> signUpDomainService.signUpByEmail(VALID_EMAIL, VALID_PASSWORD, USER))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_VERIFIED);

        then(signUpPolicy).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }
}
