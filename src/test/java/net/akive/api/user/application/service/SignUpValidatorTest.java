package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.ConflictException;

import net.akive.api.auth.application.emailverification.provided.dto.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.emailverification.provided.EnsureEmailVerifiedUseCase;
import net.akive.api.common.domain.Email;
import net.akive.api.user.application.required.ValidateUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SignUpValidatorTest {

    @Mock
    ValidateUserPort validateUserPort;

    @Mock
    EnsureEmailVerifiedUseCase ensureEmailVerifiedUseCase;

    @InjectMocks
    SignUpValidator signUpValidator;

    @Test
    void 이미_가입된_이메일이면_회원가입할_수_없다() {
        // given
        Email email = new Email("test@test.com");
        given(validateUserPort.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> signUpValidator.validateEmailSignUpAvailable(email))
                .extracting(ConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(ensureEmailVerifiedUseCase).should(never()).ensureVerified(any());
    }

    @Test
    void 이메일_중복이_아니면_인증_완료_여부를_확인한다() {
        // given
        Email email = new Email("test@test.com");
        given(validateUserPort.existsByEmail(email)).willReturn(false);

        // when
        signUpValidator.validateEmailSignUpAvailable(email);

        // then
        then(ensureEmailVerifiedUseCase)
                .should()
                .ensureVerified(new EnsureEmailVerifiedCommand(email, SIGN_UP));
    }
}
