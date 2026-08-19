package net.akive.api.auth.application.emailverification.service;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.user.application.provided.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
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
        Email email = createEmail();

        emailVerificationValidationService.validateIssuable(email, SIGN_UP);

        then(userValidator).should().validateEmailNotDuplicate(email);
        then(userValidator).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 회원가입을_위한_인증_요청_시_이미_가입된_이메일이면_예외를_던진다() {
        Email email = createEmail();
        ConflictException exception = new ConflictException(EMAIL_ALREADY_EXISTS);

        willThrow(exception).given(userValidator).validateEmailNotDuplicate(email);

        assertThatThrownBy(() -> emailVerificationValidationService.validateIssuable(email, SIGN_UP))
                .isSameAs(exception);

        then(userValidator).should().validateEmailNotDuplicate(email);
        then(userValidator).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 패스워드_리셋을_위한_이메일_인증번호_발급_가능_여부를_검증한다() {
        Email email = createEmail();

        emailVerificationValidationService.validateIssuable(email, PASSWORD_RESET);

        then(userValidator).should().validateUserExistsByEmail(email);
        then(userValidator).should(never()).validateEmailNotDuplicate(any());
    }

    @Test
    void 패스워드_리셋을_위한_이메일_인증_요청_시_회원이_없으면_예외를_던진다() {
        Email email = createEmail();
        NotFoundException exception = new NotFoundException(USER_NOT_FOUND);

        willThrow(exception).given(userValidator).validateUserExistsByEmail(email);

        assertThatThrownBy(() -> emailVerificationValidationService.validateIssuable(email, PASSWORD_RESET))
                .isSameAs(exception);

        then(userValidator).should().validateUserExistsByEmail(email);
        then(userValidator).should(never()).validateEmailNotDuplicate(any());
    }
}
