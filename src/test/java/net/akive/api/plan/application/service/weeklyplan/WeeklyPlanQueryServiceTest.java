package net.akive.api.plan.application.service.weeklyplan;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_PLAN_NOT_FOUND;
import static net.akive.api.plan.domain.PlanFixture.createPersistedSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createPersistedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
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
    void 주간_계획을_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Long totalQuantityId = 20L;
        int weekNum = 1;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        SessionWeek sessionWeek = sessionalPlan.getSessionPeriod().findSessionWeek(weekNum).orElseThrow();
        TotalQuantity totalQuantity = createPersistedTotalQuantity(totalQuantityId, sessionalPlan);
        WeeklyPlanData weeklyPlanData = createWeeklyPlanData(sessionalPlanId, totalQuantity);
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum);

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan.getSessionPeriod()));
        given(weeklyQuantityRepository.findAllWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
                sessionalPlanId,
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
                .findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(weeklyQuantityRepository).should()
                .findAllWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(sessionalPlanId, sessionWeek.weekPeriod());
    }

    @Test
    void 세셔널_플랜이_없으면_주간_계획_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        int weekNum = 1;
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum);

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> weeklyPlanQueryService.find(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(weeklyQuantityRepository).shouldHaveNoInteractions();
    }

    @Test
    void 주차가_세션_기간을_벗어나면_주간_계획_조회에_실패하고_예외를_던진다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        int weekNum = 999;
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum);

        given(sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId))
                .willReturn(Optional.of(sessionalPlan.getSessionPeriod()));

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> weeklyPlanQueryService.find(query))
                .extracting(NotFoundException::getError)
                .isEqualTo(WEEKLY_PLAN_NOT_FOUND);

        then(sessionalPlanRepository).should()
                .findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId);
        then(weeklyQuantityRepository).shouldHaveNoInteractions();
    }

    private WeeklyPlanData createWeeklyPlanData(Long sessionalPlanId, TotalQuantity totalQuantity) {
        return new WeeklyPlanData(
                sessionalPlanId,
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                totalQuantity.getWeeklyQuantities().getFirst()
        );
    }
}
