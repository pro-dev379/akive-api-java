package net.akive.api.auth.application.emailverification.service;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationValidationServiceTest {

    @Mock
    UserValidator userValidator;

    @InjectMocks
    EmailVerificationValidationService emailVerificationValidationService;

    @Test
    void 회원가입을_위한_이메일_인증번호_발급_가능_여부를_검증한다() {
        emailVerificationValidationService.validateIssuable(EMAIL, SIGN_UP);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(userValidator).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 회원가입을_위한_인증_요청_시_이미_가입된_이메일이면_예외를_전파한다() {
        ResourceConflictException exception = new ResourceConflictException(EMAIL_ALREADY_EXISTS);

        willThrow(exception).given(userValidator).validateEmailNotDuplicate(EMAIL);

        assertThatThrownBy(() -> emailVerificationValidationService.validateIssuable(EMAIL, SIGN_UP))
                .isSameAs(exception);

        then(userValidator).should().validateEmailNotDuplicate(EMAIL);
        then(userValidator).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 패스워드_리셋을_위한_이메일_인증번호_발급_가능_여부를_검증한다() {
        emailVerificationValidationService.validateIssuable(EMAIL, PASSWORD_RESET);

        then(userValidator).should().validateUserExistsByEmail(EMAIL);
        then(userValidator).should(never()).validateEmailNotDuplicate(any());
    }

    @Test
    void 패스워드_리셋을_위한_이메일_인증_요청_시_회원이_없으면_예외를_전파한다() {
        ResourceNotFoundException exception = new ResourceNotFoundException(USER_NOT_FOUND);

        willThrow(exception).given(userValidator).validateUserExistsByEmail(EMAIL);

        assertThatThrownBy(() -> emailVerificationValidationService.validateIssuable(EMAIL, PASSWORD_RESET))
                .isSameAs(exception);

        then(userValidator).should().validateUserExistsByEmail(EMAIL);
        then(userValidator).should(never()).validateEmailNotDuplicate(any());
    }
}
