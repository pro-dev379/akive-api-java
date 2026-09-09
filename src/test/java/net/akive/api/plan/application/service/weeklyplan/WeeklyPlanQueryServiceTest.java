package net.akive.api.plan.application.service.weeklyplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.value.SessionWeek;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.WEEKLY_PLAN_NOT_FOUND;
import static net.akive.api.plan.application.fixture.PlanDataFixture.createWeeklyPlanData;
import static net.akive.api.plan.application.fixture.PlanQueryFixture.createWeeklyPlanFindQuery;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WeeklyPlanQueryServiceTest {

    @Mock
    SessionalPlanRepository sessionalPlanRepository;

    @Mock
    WeeklyQuantityRepository weeklyQuantityRepository;

    @InjectMocks
    WeeklyPlanQueryService weeklyPlanQueryService;

    @Test
    void 위클리_플랜을_조회한다() {
        WeeklyPlanFindQuery query = createWeeklyPlanFindQuery(FIRST_WEEK_NUM);
        SessionWeek sessionWeek = SESSION_PERIOD.findSessionWeek(query.weekNum()).orElseThrow();
        WeeklyPlanData weeklyPlanData = createWeeklyPlanData(query.sessionalPlanId());

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(
                query.sessionalPlanId(),
                query.userId()
        )).willReturn(Optional.of(SESSION_PERIOD));
        given(weeklyQuantityRepository.findAllWeeklyPlanDataBySessionalPlanIdAndUserIdAndWeekPeriod(
                query.sessionalPlanId(),
                query.userId(),
                sessionWeek.weekPeriod()
        )).willReturn(List.of(weeklyPlanData));

        WeeklyPlan weeklyPlan = weeklyPlanQueryService.find(query);

        assertThat(weeklyPlan.sessionWeek()).isEqualTo(sessionWeek);
        assertThat(weeklyPlan.weeklyQuantities()).hasSize(1);
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem = weeklyPlan.weeklyQuantities().getFirst();
        assertThat(weeklyPlanItem.sessionalPlanId()).isEqualTo(weeklyPlanData.sessionalPlanId());
        assertThat(weeklyPlanItem.totalQuantityId()).isEqualTo(weeklyPlanData.totalQuantityId());
        assertThat(weeklyPlanItem.subject()).isEqualTo(weeklyPlanData.subject());
        assertThat(weeklyPlanItem.quantityUnit()).isEqualTo(weeklyPlanData.quantityUnit());
        assertThat(weeklyPlanItem.weeklyQuantity()).isEqualTo(weeklyPlanData.weeklyQuantity());
        assertThat(weeklyPlan.planDates()).hasSize(4);

        then(sessionalPlanRepository).should()
                .findSessionPeriodBySessionalPlanIdAndUserId(query.sessionalPlanId(), query.userId());
        then(weeklyQuantityRepository).should()
                .findAllWeeklyPlanDataBySessionalPlanIdAndUserIdAndWeekPeriod(
                        query.sessionalPlanId(),
                        query.userId(),
                        sessionWeek.weekPeriod()
                );
    }

    @Test
    void 세셔널_플랜이_없으면_위클리_플랜_조회에_실패하고_예외를_던진다() {
        WeeklyPlanFindQuery query = createWeeklyPlanFindQuery(FIRST_WEEK_NUM);

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(
                query.sessionalPlanId(),
                query.userId()
        )).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyPlanQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findSessionPeriodBySessionalPlanIdAndUserId(query.sessionalPlanId(), query.userId());
        then(weeklyQuantityRepository).shouldHaveNoInteractions();
    }

    @Test
    void 주차가_세션_기간을_벗어나면_위클리_플랜_조회에_실패하고_예외를_던진다() {
        WeeklyPlanFindQuery query = createWeeklyPlanFindQuery(UNKNOWN_WEEK_NUM);

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(
                query.sessionalPlanId(),
                query.userId()
        )).willReturn(Optional.of(SESSION_PERIOD));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyPlanQueryService.find(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findSessionPeriodBySessionalPlanIdAndUserId(query.sessionalPlanId(), query.userId());
        then(weeklyQuantityRepository).shouldHaveNoInteractions();
    }
}
