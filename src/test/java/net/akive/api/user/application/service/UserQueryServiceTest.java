package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회해서_반환한다() {
        User user = createReconstructedUser();
        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));

        User foundUser = userQueryService.findOrThrowByUserIdAndNotWithdrawn(USER_ID);

        assertThat(foundUser).isEqualTo(user);

        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회할_수_없으면_예외가_발생한다() {
        given(userRepository.findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> userQueryService.findOrThrowByUserIdAndNotWithdrawn(UNKNOWN_USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID);
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_Optional로_조회한다() {
        User user = createReconstructedUser();
        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));

        Optional<User> foundUser = userQueryService.findByUserIdAndNotWithdrawn(USER_ID);

        assertThat(foundUser).contains(user);

        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_Optional로_조회한다() {
        User user = createReconstructedUser();
        given(userRepository.findByEmailAndNotWithdrawn(EMAIL)).willReturn(Optional.of(user));

        Optional<User> foundUser = userQueryService.findByEmailAndNotWithdrawn(EMAIL);

        assertThat(foundUser).contains(user);

        then(userRepository).should().findByEmailAndNotWithdrawn(EMAIL);
    }
}
