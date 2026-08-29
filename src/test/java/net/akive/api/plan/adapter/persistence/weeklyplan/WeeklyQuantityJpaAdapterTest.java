package net.akive.api.plan.adapter.persistence.weeklyplan;

import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyQuantityJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 주간_계획을_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        flushAndClear();

        List<WeeklyPlanData> weeklyPlanItems = weeklyQuantityRepository
                .findAllWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
                        savedTotalQuantity.getSessionalPlan().getSessionalPlanId(),
                        weekPeriod
                );

        assertThat(weeklyPlanItems).hasSize(1);
        WeeklyPlanData weeklyPlan = weeklyPlanItems.getFirst();
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

    @Test
    void 주간_계획_내_특정_주간_계획량을_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        Long sessionalPlanId = savedTotalQuantity.getSessionalPlan().getSessionalPlanId();
        Long userId = savedTotalQuantity.getSessionalPlan().getUser().getUserId();
        Long totalQuantityId = savedTotalQuantity.getTotalQuantityId();
        WeeklyQuantity savedWeeklyQuantity = savedTotalQuantity.getWeeklyQuantities().getFirst();
        Long weeklyQuantityId = savedWeeklyQuantity.getWeeklyQuantityId();
        flushAndClear();

        Optional<WeeklyPlanData> weeklyPlan = weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
                );

        assertThat(weeklyPlan).isPresent();
        WeeklyPlanData weeklyPlanData = weeklyPlan.get();
        assertThat(weeklyPlanData.sessionalPlanId()).isEqualTo(sessionalPlanId);
        assertThat(weeklyPlanData.totalQuantityId()).isEqualTo(totalQuantityId);
        assertThat(weeklyPlanData.subject()).isEqualTo(savedTotalQuantity.getSubject());
        assertThat(weeklyPlanData.quantityUnit()).isEqualTo(savedTotalQuantity.getTotalQuantityAmount().quantityUnit());
        assertThat(weeklyPlanData.weeklyQuantity().getWeeklyQuantityId()).isEqualTo(weeklyQuantityId);
        assertThat(weeklyPlanData.weeklyQuantity().getWeekPeriod()).isEqualTo(savedWeeklyQuantity.getWeekPeriod());
        assertThat(weeklyPlanData.weeklyQuantity().getDailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3),
                        LocalDate.of(2026, 1, 4)
                );
    }
}
