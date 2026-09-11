package net.akive.api.plan.adapter.persistence.dailyplan;

import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DailyQuantityJpaAdapterTest extends RepositoryTestSupport {

    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate UNKNOWN_DATE = LocalDate.of(2027, 1, 1);

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
        assertThat(dailyPlan.weeklyQuantityId())
                .isEqualTo(savedTotalQuantity.getWeeklyQuantities().getFirst().getWeeklyQuantityId());
        assertThat(dailyPlan.subject()).isEqualTo(savedTotalQuantity.getSubject());
        assertThat(dailyPlan.quantityUnit()).isEqualTo(savedTotalQuantity.getTotalQuantityAmount().quantityUnit());
        assertThat(dailyPlan.dailyQuantity().getDate()).isEqualTo(QUERY_DATE);
        assertThat(dailyPlan.dailyQuantity().getDailyQuantityAmount().quantityAmount().value()).isEqualTo(100);
    }

    @Test
    void 다른_회원의_일일_계획량은_조회하지_않는다() {
        saveTotalQuantity();
        flushAndClear();

        List<DailyPlanData> dailyPlans = dailyQuantityRepository.findAllDailyPlanDataByUserIdAndDate(
                UNKNOWN_USER_ID,
                QUERY_DATE
        );

        assertThat(dailyPlans).isEmpty();
    }

    @Test
    void 날짜가_다르면_일일_계획량을_조회하지_않는다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        List<DailyPlanData> dailyPlans = dailyQuantityRepository.findAllDailyPlanDataByUserIdAndDate(
                savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                UNKNOWN_DATE
        );

        assertThat(dailyPlans).isEmpty();
    }
}
