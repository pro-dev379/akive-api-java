package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.GOAL_NOT_FOUND;
import static net.akive.api.user.domain.fixture.GoalFixture.GOAL_ID;
import static net.akive.api.user.domain.fixture.GoalFixture.createReconstructedGoal;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    GoalRepository goalRepository;

    @InjectMocks
    GoalService goalService;

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원의_최신_목표를_조회한다() {
        Goal goal = createReconstructedGoal(GOAL_ID);
        given(goalRepository.findLatestByUserIdAndUserNotWithdrawn(USER_ID)).willReturn(Optional.of(goal));

        Goal foundGoal = goalService.findLatestByUserIdAndUserNotWithdrawn(USER_ID);

        assertThat(foundGoal).isEqualTo(goal);
        then(goalRepository).should().findLatestByUserIdAndUserNotWithdrawn(USER_ID);
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원의_최신_목표를_조회할_수_없으면_예외가_발생한다() {
        given(goalRepository.findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> goalService.findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(GOAL_NOT_FOUND);

        then(goalRepository).should().findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID);
    }
}
