package net.akive.api.user.application.service;

import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.user.application.required.LoadUserPort;
import net.akive.api.user.application.required.SaveUserPort;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
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

        User user = User.reconstruct(
                userId,
                new Email("test@test.com"),
                USER,
                EMAIL,
                "USER@abcde",
                "old-password",
                null,
                null,
                false,
                null
        );

        given(loadUserPort.loadByUserIdAndLeaveFalse(userId))
                .willReturn(Optional.of(user));

        // when
        accountService.updatePassword(userId, encodedPassword);

        // then
        assertThat(user.getPasswordHash()).isEqualTo(encodedPassword);

        then(saveUserPort).should().save(user);
    }

    @Test
    void 회원이_존재하지_않으면_패스워드_변경이_실패한다() {
        // given
        Long userId = 999L;

        given(loadUserPort.loadByUserIdAndLeaveFalse(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> accountService.updatePassword(userId, "encoded-password"))
                .extracting(NotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(saveUserPort).shouldHaveNoInteractions();
    }
}
