package net.akive.api.plan.adapter.persistence.weeklyplan;

import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyPlanJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 세셔널_플랜_식별자와_주차_기간으로_주간_계획을_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        flushAndClear();

        List<WeeklyPlanData> weeklyPlans = weeklyPlanRepository
                .findWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
                        savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                        weekPeriod
                );

        assertThat(weeklyPlans).hasSize(1);
        WeeklyPlanData weeklyPlan = weeklyPlans.getFirst();
        assertThat(weeklyPlan.sessionalPlanId())
                .isEqualTo(savedTotalQuantity.getSessionalPlan().getSessionalPlanId());
        assertThat(weeklyPlan.totalQuantityId()).isEqualTo(savedTotalQuantity.getTotalQuantityId());
        assertThat(weeklyPlan.subject()).isEqualTo(savedTotalQuantity.getSubject());
        assertThat(weeklyPlan.quantityUnit()).isEqualTo(savedTotalQuantity.getTotalQuantityAmount().quantityUnit());
        assertThat(weeklyPlan.weeklyQuantity().getWeekPeriod()).isEqualTo(weekPeriod);
        assertThat(weeklyPlan.weeklyQuantity().getDailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3),
                        LocalDate.of(2026, 1, 4)
                );
    }
}
