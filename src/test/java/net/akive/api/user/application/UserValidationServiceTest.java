package net.akive.api.user.application;

import net.akive.api.user.application.port.out.ValidateUserPort;
import net.akive.api.user.excpetion.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.user.excpetion.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static net.akive.api.user.excpetion.UserErrorCode.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserValidationServiceTest {

    @Mock
    ValidateUserPort validateUserPort;

    @InjectMocks
    UserValidationService userValidationService;

    @Test
    void 이메일로_회원_존재_검증에_성공한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmailAndLeaveFalse(email)).willReturn(true);

        // when
        userValidationService.validateUserExistsByEmail(email);

        // then
        then(validateUserPort).should().existsByEmailAndLeaveFalse(email);
    }

    @Test
    void 이메일에_해당하는_회원이_없으면_회원_존재_검증이_실패한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmailAndLeaveFalse(email)).willReturn(false);

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> userValidationService.validateUserExistsByEmail(email))
                .extracting(UserException::getErrorCode)
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void 식별값으로_회원_존재_검증에_성공한다() {
        // given
        Long userId = 1L;
        given(validateUserPort.existsByUserIdAndLeaveFalse(userId)).willReturn(true);

        // when
        userValidationService.validateUserExistsById(userId);

        // then
        then(validateUserPort).should().existsByUserIdAndLeaveFalse(userId);
    }

    @Test
    void 식별값에_해당하는_회원이_없으면_회원_존재_검증이_실패한다() {
        // given
        Long userId = 1L;
        given(validateUserPort.existsByUserIdAndLeaveFalse(userId)).willReturn(false);

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> userValidationService.validateUserExistsById(userId))
                .extracting(UserException::getErrorCode)
                .isEqualTo(NOT_FOUND);
    }

    @Test
    void 이메일_중복_검증에_성공한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmail(email)).willReturn(false);

        // when
        userValidationService.validateEmailNotDuplicate(email);

        // then
        then(validateUserPort).should().existsByEmail(email);
    }

    @Test
    void 이미_존재하는_이메일이면_이메일_중복_검증이_실패한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> userValidationService.validateEmailNotDuplicate(email))
                .extracting(UserException::getErrorCode)
                .isEqualTo(EMAIL_ALREADY_EXISTS);
    }
}
