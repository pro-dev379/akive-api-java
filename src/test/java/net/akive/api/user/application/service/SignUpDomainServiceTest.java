package net.akive.api.user.application.service;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.ConflictError;
import net.akive.api.user.application.port.out.GenerateNicknamePort;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_NOT_VERIFIED;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SignUpDomainServiceTest {

    private static final Email VALID_EMAIL = new Email("test@test.com");
    private static final String VALID_PASSWORD = "password123!";

    @Mock
    SignUpValidator signUpValidator;

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

        then(signUpValidator).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).should().generate(USER);
        then(passwordEncoder).should().encode(VALID_PASSWORD);
    }

    @Test
    void 이미_가입된_이메일이면_회원_생성에_실패한다() {
        // given
        willThrow(new ConflictException(ConflictError.EMAIL_ALREADY_EXISTS))
                .given(signUpValidator)
                .validateEmailSignUpAvailable(VALID_EMAIL);

        // when & then
        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> signUpDomainService.signUpByEmail(VALID_EMAIL, VALID_PASSWORD, USER))
                .extracting(ConflictException::getError)
                .isEqualTo(ConflictError.EMAIL_ALREADY_EXISTS);

        then(signUpValidator).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_인증이_완료되지_않으면_회원_생성에_실패한다() {
        // given
        willThrow(new BadRequestException(EMAIL_VERIFICATION_NOT_VERIFIED))
                .given(signUpValidator)
                .validateEmailSignUpAvailable(VALID_EMAIL);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> signUpDomainService.signUpByEmail(VALID_EMAIL, VALID_PASSWORD, USER))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_VERIFIED);

        then(signUpValidator).should().validateEmailSignUpAvailable(VALID_EMAIL);
        then(generateNicknamePort).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }
}
