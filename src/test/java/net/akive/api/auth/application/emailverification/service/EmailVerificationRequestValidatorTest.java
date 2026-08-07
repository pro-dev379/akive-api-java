package net.akive.api.auth.application.emailverification.service;

import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;

import net.akive.api.common.domain.Email;
import net.akive.api.user.application.provided.ValidateUserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.FIND_PASSWORD;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationRequestValidatorTest {

    @Mock
    ValidateUserUseCase validateUserUseCase;

    @InjectMocks
    EmailVerificationRequestValidator emailVerificationRequestValidator;

    @Test
    void 회원가입_인증은_이메일_중복을_검증한다() {
        // when
        Email email = new Email("test@test.com");

        emailVerificationRequestValidator.validateRequestAvailable(email, SIGN_UP);

        // then
        then(validateUserUseCase).should().validateEmailNotDuplicate(email);
        then(validateUserUseCase).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 회원가입_인증_요청_시_이미_가입된_이메일이면_요청할_수_없다() {
        // given
        willThrow(new ConflictException(EMAIL_ALREADY_EXISTS))
                .given(validateUserUseCase)
                .validateEmailNotDuplicate(new Email("test@test.com"));

        // when & then
        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() ->
                        emailVerificationRequestValidator.validateRequestAvailable(
                                new Email("test@test.com"),
                                SIGN_UP
                        )
                )
                .extracting(ConflictException::getError)
                .isEqualTo(EMAIL_ALREADY_EXISTS);

        then(validateUserUseCase).should().validateEmailNotDuplicate(new Email("test@test.com"));
        then(validateUserUseCase).should(never()).validateUserExistsByEmail(any());
    }

    @Test
    void 패스워드_찾기_인증은_기존_회원을_검증한다() {
        // when
        Email email = new Email("test@test.com");

        emailVerificationRequestValidator.validateRequestAvailable(email, FIND_PASSWORD);

        // then
        then(validateUserUseCase).should().validateUserExistsByEmail(email);
        then(validateUserUseCase).should(never()).validateEmailNotDuplicate(any());
    }

    @Test
    void 패스워드_찾기_인증_요청_시_회원이_없으면_요청할_수_없다() {
        // given
        willThrow(new NotFoundException(USER_NOT_FOUND))
                .given(validateUserUseCase)
                .validateUserExistsByEmail(new Email("test@test.com"));

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() ->
                        emailVerificationRequestValidator.validateRequestAvailable(
                                new Email("test@test.com"),
                                FIND_PASSWORD
                        )
                )
                .extracting(NotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(validateUserUseCase).should().validateUserExistsByEmail(new Email("test@test.com"));
        then(validateUserUseCase).should(never()).validateEmailNotDuplicate(any());
    }
}
