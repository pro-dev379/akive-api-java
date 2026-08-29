package net.akive.api.plan.adapter.persistence.dailyplan;

import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyPlanJpaAdapterTest extends RepositoryTestSupport {

    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 1, 1);

    @Test
    void 회원_식별자와_날짜로_일일_계획량을_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        List<DailyPlanData> dailyPlans = dailyQuantityRepository.findAllDailyPlanDataByUserIdAndDate(
                savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                QUERY_DATE
        );

        assertThat(dailyPlans).hasSize(1);
        DailyPlanData dailyPlan = dailyPlans.getFirst();
        assertThat(dailyPlan.sessionalPlanId())
                .isEqualTo(savedTotalQuantity.getSessionalPlan().getSessionalPlanId());
        assertThat(dailyPlan.totalQuantityId()).isEqualTo(savedTotalQuantity.getTotalQuantityId());
        assertThat(dailyPlan.subject()).isEqualTo(savedTotalQuantity.getSubject());
        assertThat(dailyPlan.quantityUnit()).isEqualTo(savedTotalQuantity.getTotalQuantityAmount().quantityUnit());
        assertThat(dailyPlan.dailyQuantity().getDate()).isEqualTo(QUERY_DATE);
        assertThat(dailyPlan.dailyQuantity().getDailyQuantityAmount()
                .quantityAmount()
                .value()).isEqualTo(100);
    }

}
