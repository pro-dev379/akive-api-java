package net.akive.api.user.application;

import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.port.in.emailverification.EnsureEmailVerifiedUseCase;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.exception.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.exception.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SignUpPolicyTest {

    @Mock
    ValidateUserPort validateUserPort;

    @Mock
    EnsureEmailVerifiedUseCase ensureEmailVerifiedUseCase;

    @InjectMocks
    SignUpPolicy signUpPolicy;

    @Test
    void 이미_가입된_이메일이면_회원가입할_수_없다() {
        // given
        Email email = Email.of("test@test.com");
        given(validateUserPort.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> signUpPolicy.validateEmailSignUpAvailable(email))
                .extracting(UserException::getErrorCode)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(ensureEmailVerifiedUseCase).should(never()).ensureVerified(any());
    }

    @Test
    void 이메일_중복이_아니면_인증_완료_여부를_확인한다() {
        // given
        Email email = Email.of("test@test.com");
        given(validateUserPort.existsByEmail(email)).willReturn(false);

        // when
        signUpPolicy.validateEmailSignUpAvailable(email);

        // then
        then(ensureEmailVerifiedUseCase)
                .should()
                .ensureVerified(new EnsureEmailVerifiedCommand(email, SIGN_UP));
    }
}
