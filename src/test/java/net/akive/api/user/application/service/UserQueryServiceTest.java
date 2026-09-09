package net.akive.api.user.application.service;

import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.user.domain.UserFixture.*;
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
        User user = createReconstructedUser();
        given(userRepository.findByUserIdAndLeaveFalse(USER_ID)).willReturn(Optional.of(user));

        Optional<User> found = userQueryService.findByUserIdAndLeaveFalse(USER_ID);

        assertThat(found).contains(user);

        then(userRepository).should().findByUserIdAndLeaveFalse(USER_ID);
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_조회한다() {
        User user = createReconstructedUser();
        given(userRepository.findByEmailAndLeaveFalse(EMAIL)).willReturn(Optional.of(user));

        Optional<User> found = userQueryService.findByEmailAndLeaveFalse(EMAIL);

        assertThat(found).contains(user);

        then(userRepository).should().findByEmailAndLeaveFalse(EMAIL);
    }
}
