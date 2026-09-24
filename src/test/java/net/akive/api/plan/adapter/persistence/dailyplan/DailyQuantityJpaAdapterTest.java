package net.akive.api.plan.adapter.persistence.dailyplan;

import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityCompletionAmount;
import net.akive.api.plan.domain.value.TotalQuantityDailyCompletionAmount;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DailyQuantityJpaAdapterTest extends RepositoryTestSupport {

    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate UNKNOWN_DATE = LocalDate.of(2027, 1, 1);

    @Test
    void 회원_식별자와_날짜로_일일_계획량을_조회한다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        List<DailyPlanData> dailyPlans =
                dailyQuantityRepository.findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
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

        List<DailyPlanData> dailyPlans =
                dailyQuantityRepository.findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
                        UNKNOWN_USER_ID,
                        QUERY_DATE
                );

        assertThat(dailyPlans).isEmpty();
    }

    @Test
    void 날짜가_다르면_일일_계획량을_조회하지_않는다() {
        TotalQuantity savedTotalQuantity = saveTotalQuantity();
        flushAndClear();

        List<DailyPlanData> dailyPlans =
                dailyQuantityRepository.findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
                        savedTotalQuantity.getSessionalPlan().getUser().getUserId(),
                        UNKNOWN_DATE
                );

        assertThat(dailyPlans).isEmpty();
    }

    @Test
    void 조회_기간에_해당하는_계획량과_완료량_각각을_세션_총량별로_합산한다() {
        int firstCompletedAmount = 40;
        int secondCompletedAmount = 80;
        SessionalPlan sessionalPlan = saveSessionalPlan();
        completeFirstDailyQuantity(
                saveTotalQuantity(sessionalPlan, FIRST_SUBJECT_NAME, TOTAL_AMOUNT),
                firstCompletedAmount
        );
        completeFirstDailyQuantity(
                saveTotalQuantity(sessionalPlan, SECOND_SUBJECT_NAME, TOTAL_AMOUNT),
                secondCompletedAmount
        );
        flushAndClear();

        List<QuantityCompletionAmount> completionAmounts =
                dailyQuantityRepository.findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
                        sessionalPlan.getUser().getUserId(),
                        QUERY_DATE,
                        QUERY_DATE
                );

        assertThat(completionAmounts).containsExactly(
                new QuantityCompletionAmount(TOTAL_AMOUNT, firstCompletedAmount),
                new QuantityCompletionAmount(TOTAL_AMOUNT, secondCompletedAmount)
        );
    }

    @Test
    void 조회_기간에_해당하는_계획량과_완료량을_날짜와_세션_총량별로_합산한다() {
        int completedAmount = 40;
        SessionalPlan sessionalPlan = saveSessionalPlan();
        TotalQuantity totalQuantity = completeFirstDailyQuantity(
                saveTotalQuantity(sessionalPlan, FIRST_SUBJECT_NAME, TOTAL_AMOUNT),
                completedAmount
        );
        flushAndClear();

        List<TotalQuantityDailyCompletionAmount> dailyCompletionAmounts =
                dailyQuantityRepository.findTotalQuantityDailyCompletionAmountsByUserIdAndDateBetween(
                        sessionalPlan.getUser().getUserId(),
                        QUERY_DATE,
                        QUERY_DATE
                );

        assertThat(dailyCompletionAmounts).containsExactly(new TotalQuantityDailyCompletionAmount(
                totalQuantity.getTotalQuantityId(),
                QUERY_DATE,
                new QuantityCompletionAmount(TOTAL_AMOUNT, completedAmount)
        ));
    }

    private TotalQuantity completeFirstDailyQuantity(TotalQuantity totalQuantity, int completedAmount) {
        WeeklyQuantity firstWeeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        DailyQuantity firstDailyQuantity = firstWeeklyQuantity.getDailyQuantities().getFirst();
        totalQuantity.updateDailyCompletedAmount(
                firstWeeklyQuantity.getWeeklyQuantityId(),
                firstDailyQuantity.getDailyQuantityId(),
                new QuantityAmount(completedAmount)
        );

        return totalQuantityRepository.save(totalQuantity);
    }
}
