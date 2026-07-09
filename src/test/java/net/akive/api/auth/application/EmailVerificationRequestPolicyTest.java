package net.akive.api.auth.application;

import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import net.akive.api.user.exception.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.FIND_PASSWORD;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.exception.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static net.akive.api.user.exception.UserErrorCode.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationRequestPolicyTest {

    @Mock
    ValidateUserUseCase validateUserUseCase;

    @InjectMocks
    EmailVerificationRequestPolicy emailVerificationRequestPolicy;

    @Test
    void 회원가입_인증은_이메일_중복을_검증한다() {
        // when
        Email email = Email.of("test@test.com");

        emailVerificationRequestPolicy.validateRequestAvailable(email, SIGN_UP);

        // then
        then(validateUserUseCase).should().validateEmailNotDuplicate(email);
        then(validateUserUseCase).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 회원가입_인증_요청_시_이미_가입된_이메일이면_요청할_수_없다() {
        // given
        willThrow(new UserException(EMAIL_ALREADY_EXISTS))
                .given(validateUserUseCase)
                .validateEmailNotDuplicate(Email.of("test@test.com"));

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() ->
                        emailVerificationRequestPolicy.validateRequestAvailable(
                                Email.of("test@test.com"),
                                SIGN_UP
                        )
                )
                .extracting(UserException::getErrorCode)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(validateUserUseCase).should().validateEmailNotDuplicate(Email.of("test@test.com"));
        then(validateUserUseCase).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 패스워드_찾기_인증은_기존_회원을_검증한다() {
        // when
        Email email = Email.of("test@test.com");

        emailVerificationRequestPolicy.validateRequestAvailable(email, FIND_PASSWORD);

        // then
        then(validateUserUseCase).should().validateUserExistsByEmail(email);
        then(validateUserUseCase).should(never()).validateEmailNotDuplicate(any());
    }

    @Test
    void 패스워드_찾기_인증_요청_시_회원이_없으면_요청할_수_없다() {
        // given
        willThrow(new UserException(NOT_FOUND))
                .given(validateUserUseCase)
                .validateUserExistsByEmail(Email.of("test@test.com"));

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() ->
                        emailVerificationRequestPolicy.validateRequestAvailable(
                                Email.of("test@test.com"),
                                FIND_PASSWORD
                        )
                )
                .extracting(UserException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(validateUserUseCase).should().validateUserExistsByEmail(Email.of("test@test.com"));
        then(validateUserUseCase).should(never()).validateEmailNotDuplicate(any());
    }
}
