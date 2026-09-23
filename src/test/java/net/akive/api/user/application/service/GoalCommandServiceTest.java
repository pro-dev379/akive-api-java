package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.dto.GoalCreateCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.application.fixture.GoalCommandFixture.createGoalCreateCommand;
import static net.akive.api.user.domain.fixture.GoalFixture.GOAL_ID;
import static net.akive.api.user.domain.fixture.GoalFixture.createReconstructedGoal;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class GoalCommandServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    GoalRepository goalRepository;

    @InjectMocks
    GoalCommandService goalCommandService;

    @Test
    void 탈퇴하지_않은_회원의_최신_목표를_추가한다() {
        User user = createReconstructedUser(USER_ID);
        GoalCreateCommand command = createGoalCreateCommand();
        Goal savedGoal = createReconstructedGoal(GOAL_ID);

        given(userRepository.findByUserIdAndNotWithdrawn(USER_ID)).willReturn(Optional.of(user));
        given(goalRepository.save(any(Goal.class))).willReturn(savedGoal);

        Goal result = goalCommandService.create(command);

        assertThat(result).isEqualTo(savedGoal);
        then(userRepository).should().findByUserIdAndNotWithdrawn(USER_ID);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        then(goalRepository).should().save(goalCaptor.capture());
        Goal goalToSave = goalCaptor.getValue();
        assertThat(goalToSave.getGoalId()).isNull();
        assertThat(goalToSave.getUser()).isEqualTo(user);
        assertThat(goalToSave.getAttendOn()).isEqualTo(command.attendOn());
        assertThat(goalToSave.getRest10MinSec()).isEqualTo(command.rest10MinSec());
        assertThat(goalToSave.getMealNum()).isEqualTo(command.mealNum());
        assertThat(goalToSave.getMeal1TimeSec()).isEqualTo(command.meal1TimeSec());
    }

    @Test
    void 최신_목표_추가_시_탈퇴하지_않은_회원을_조회할_수_없으면_예외가_발생한다() {
        GoalCreateCommand command = createGoalCreateCommand(UNKNOWN_USER_ID);
        given(userRepository.findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> goalCommandService.create(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userRepository).should().findByUserIdAndNotWithdrawn(UNKNOWN_USER_ID);
        then(goalRepository).should(never()).save(any(Goal.class));
    }
}
