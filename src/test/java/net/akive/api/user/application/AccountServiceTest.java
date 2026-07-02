package net.akive.api.user.application;

import net.akive.api.user.application.port.out.LoadUserPort;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.User;
import net.akive.api.user.excpetion.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.user.domain.RoleType.USER;
import static net.akive.api.user.excpetion.UserErrorCode.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    SaveUserPort saveUserPort;

    @Mock
    LoadUserPort loadUserPort;

    @InjectMocks
    AccountService accountService;

    @Test
    void 탈퇴하지_않은_회원이라면_전달받은_암호화된_패스워드로_변경하고_저장한다() {
        // given
        Long userId = 1L;
        String encodedPassword = "encoded-password";

        User user = User.builder()
                .userId(userId)
                .email("test@test.com")
                .nickname("USER@abcde")
                .password("old-password")
                .roleType(USER)
                .build();

        given(loadUserPort.findByUserIdAndLeaveFalse(userId))
                .willReturn(Optional.of(user));

        // when
        accountService.updatePassword(userId, encodedPassword);

        // then
        assertThat(user.getPassword()).isEqualTo(encodedPassword);

        then(saveUserPort).should().save(user);
    }

    @Test
    void 회원이_존재하지_않으면_패스워드_변경이_실패한다() {
        // given
        Long userId = 999L;

        given(loadUserPort.findByUserIdAndLeaveFalse(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> accountService.updatePassword(userId, "encoded-password"))
                .extracting(UserException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(saveUserPort).shouldHaveNoInteractions();
    }
}
