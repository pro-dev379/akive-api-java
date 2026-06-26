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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void 이메일로_회원_존재_검증_시_회원가_없으면_예외가_발생한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmailAndLeaveFalse(email)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userValidationService.validateUserExistsByEmail(email))
                .isInstanceOf(UserException.class)
                .satisfies(e ->
                        assertThat(((UserException) e).getErrorCode())
                                .isEqualTo(NOT_FOUND)
                );
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
    void 식별값으로_회원_존재_검증_시_회원가_없으면_예외가_발생한다() {
        // given
        Long userId = 1L;
        given(validateUserPort.existsByUserIdAndLeaveFalse(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userValidationService.validateUserExistsById(userId))
                .isInstanceOf(UserException.class)
                .satisfies(e ->
                        assertThat(((UserException) e).getErrorCode())
                                .isEqualTo(NOT_FOUND)
                );
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
    void 이메일_중복_검증_시_이미_존재하는_이메일이면_예외가_발생한다() {
        // given
        String email = "test@example.com";
        given(validateUserPort.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userValidationService.validateEmailNotDuplicate(email))
                .isInstanceOf(UserException.class)
                .satisfies(e ->
                        assertThat(((UserException) e).getErrorCode())
                                .isEqualTo(EMAIL_ALREADY_EXISTS)
                );
    }
}