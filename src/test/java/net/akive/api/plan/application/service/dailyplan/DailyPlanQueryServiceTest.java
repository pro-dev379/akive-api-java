package net.akive.api.plan.application.service.dailyplan;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.required.DailyPlanRepository;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.application.result.DailyPlan;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.PlanFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DailyPlanQueryServiceTest {

    @Mock
    DailyPlanRepository dailyPlanRepository;

    @InjectMocks
    DailyPlanQueryService dailyPlanQueryService;

    @Test
    void 회원_식별자와_날짜로_일일_계획을_조회한다() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);
        DailyPlanData dailyPlanData = createDailyPlanData(date);
        given(dailyPlanRepository.findDailyPlanDataByUserIdAndDate(userId, date))
                .willReturn(List.of(dailyPlanData));

        List<DailyPlan> dailyPlans = dailyPlanQueryService.find(new DailyPlansFindQuery(userId, date));

        assertThat(dailyPlans).hasSize(1);
        DailyPlan dailyPlan = dailyPlans.getFirst();
        assertThat(dailyPlan.sessionalPlanId()).isEqualTo(dailyPlanData.sessionalPlanId());
        assertThat(dailyPlan.totalQuantityId()).isEqualTo(dailyPlanData.totalQuantityId());
        assertThat(dailyPlan.weeklyQuantityId()).isEqualTo(dailyPlanData.weeklyQuantityId());
        assertThat(dailyPlan.subject()).isEqualTo(dailyPlanData.subject());
        assertThat(dailyPlan.dailyQuantity()).isEqualTo(dailyPlanData.dailyQuantity());
        assertThat(dailyPlan.quantityUnit()).isEqualTo(dailyPlanData.quantityUnit());

        then(dailyPlanRepository).should().findDailyPlanDataByUserIdAndDate(userId, date);
    }

    private DailyPlanData createDailyPlanData(LocalDate date) {
        return new DailyPlanData(
                1L,
                2L,
                3L,
                new Subject(SUBJECT_NAME, SUBJECT_CATEGORY),
                DailyQuantity.reconstruct(
                        4L,
                        date,
                        DailyQuantityAmount.of(100, 30, 70, null)
                ),
                new QuantityUnit(QUANTITY_UNIT)
        );
    }
}
