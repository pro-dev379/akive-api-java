package net.akive.api.user.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.dto.GoalVersionsFindQuery;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_DATE_RANGE;
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
class GoalQueryServiceTest {

    @Mock
    GoalRepository goalRepository;

    @InjectMocks
    GoalQueryService goalQueryService;

    @Test
    void 최신_목표를_조회한다() {
        Goal goal = createReconstructedGoal(GOAL_ID);
        given(goalRepository.findLatestByUserIdAndUserNotWithdrawn(USER_ID)).willReturn(Optional.of(goal));

        Goal foundGoal = goalQueryService.findLatest(USER_ID);

        assertThat(foundGoal).isEqualTo(goal);
        then(goalRepository).should().findLatestByUserIdAndUserNotWithdrawn(USER_ID);
    }

    @Test
    void 최신_목표를_조회할_수_없으면_예외가_발생한다() {
        given(goalRepository.findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> goalQueryService.findLatest(UNKNOWN_USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(GOAL_NOT_FOUND);

        then(goalRepository).should().findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID);
    }

    @Test
    void 특정_날짜_이전에_생성된_최신_목표를_조회한다() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        Goal goal = createReconstructedGoal(GOAL_ID);
        given(goalRepository.findLatestByUserIdAndCreatedAtBefore(USER_ID, date))
                .willReturn(Optional.of(goal));

        Goal foundGoal = goalQueryService.findLatestBefore(USER_ID, date);

        assertThat(foundGoal).isEqualTo(goal);
        then(goalRepository).should().findLatestByUserIdAndCreatedAtBefore(USER_ID, date);
    }

    @Test
    void 특정_날짜_이전에_생성된_최신_목표를_조회할_수_없으면_예외가_발생한다() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        given(goalRepository.findLatestByUserIdAndCreatedAtBefore(USER_ID, date))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> goalQueryService.findLatestBefore(USER_ID, date))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(GOAL_NOT_FOUND);

        then(goalRepository).should().findLatestByUserIdAndCreatedAtBefore(USER_ID, date);
    }

    @Test
    void 조회_기간에_적용되는_목표_이력을_조회한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 14);
        LocalDate endOn = startOn.plusDays(6);
        GoalVersionsFindQuery query = new GoalVersionsFindQuery(USER_ID, startOn, endOn);
        List<GoalVersion> goalVersions = List.of(new GoalVersion(600, 2, 1800, startOn.atStartOfDay()));
        given(goalRepository.findVersionsForPeriod(USER_ID, startOn, endOn)).willReturn(goalVersions);

        List<GoalVersion> foundVersions = goalQueryService.findVersionsForPeriod(query);

        assertThat(foundVersions).isEqualTo(goalVersions);
        then(goalRepository).should().findVersionsForPeriod(USER_ID, startOn, endOn);
    }

    @Test
    void 조회_시작일이_종료일보다_늦으면_목표_이력_조회_쿼리를_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 9, 20);
        LocalDate endOn = startOn.minusDays(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new GoalVersionsFindQuery(USER_ID, startOn, endOn))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_DATE_RANGE);

        then(goalRepository).shouldHaveNoInteractions();
    }
}
