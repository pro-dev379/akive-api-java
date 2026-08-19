package net.akive.api.user.application.service;

import net.akive.api.common.domain.Email;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회한다() {
        Long userId = 1L;
        User user = createPersistedUser(userId);
        given(userRepository.findByUserIdAndLeaveFalse(userId)).willReturn(Optional.of(user));

        Optional<User> found = userQueryService.findByUserIdAndLeaveFalse(userId);

        assertThat(found).contains(user);

        then(userRepository).should().findByUserIdAndLeaveFalse(userId);
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_조회한다() {
        Email email = createEmail();
        User user = createPersistedUser(1L);
        given(userRepository.findByEmailAndLeaveFalse(email)).willReturn(Optional.of(user));

        Optional<User> found = userQueryService.findByEmailAndLeaveFalse(email);

        assertThat(found).contains(user);

        then(userRepository).should().findByEmailAndLeaveFalse(email);
    }
}