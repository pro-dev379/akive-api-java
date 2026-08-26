package net.akive.api.plan.domain.model;

import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.PlanFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class TotalQuantityTest {

    @Test
    void 세션_총량을_생성한다() {
        SessionalPlan sessionalPlan = createSessionalPlan();
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();

        TotalQuantity totalQuantity = TotalQuantity.create(sessionalPlan, subject, totalQuantityAmount);

        assertThat(totalQuantity.getTotalQuantityId()).isNull();
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantity.getSubject()).isEqualTo(subject);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
    }

    @Test
    void 세션_기간의_주차_수만큼_주간_계획량을_생성한다() {
        SessionalPlan sessionalPlan = createSessionalPlan();

        TotalQuantity totalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity.getWeeklyQuantities()).hasSize(3);
        assertThat(totalQuantity.getWeeklyQuantities())
                .extracting(WeeklyQuantity::getWeekPeriod)
                .containsExactly(
                        new WeekPeriod(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 4)
                        ),
                        new WeekPeriod(
                                LocalDate.of(2026, 1, 5),
                                LocalDate.of(2026, 1, 11)
                        ),
                        new WeekPeriod(
                                LocalDate.of(2026, 1, 12),
                                LocalDate.of(2026, 1, 12)
                        )
                );
    }

    @Test
    void 세션_총량을_생성하면_첫_주에_전체_계획량_배정한다() {
        TotalQuantity totalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity.getWeeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount()
                        .quantityAmount()
                        .value())
                .containsExactly(TOTAL_AMOUNT, 0, 0);
    }

    @Test
    void 각_주간_계획량의_일일_계획량은_주차_기간의_날짜별로_생성된다() {
        TotalQuantity totalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );

        List<WeeklyQuantity> weeklyQuantities = totalQuantity.getWeeklyQuantities();

        assertThat(weeklyQuantities.get(0).getDailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3),
                        LocalDate.of(2026, 1, 4)
                );
        assertThat(weeklyQuantities.get(1).getDailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 1, 6),
                        LocalDate.of(2026, 1, 7),
                        LocalDate.of(2026, 1, 8),
                        LocalDate.of(2026, 1, 9),
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 11)
                );
        assertThat(weeklyQuantities.get(2).getDailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(LocalDate.of(2026, 1, 12));
    }

    @Test
    void 세션_총량을_생성하면_첫_주의_첫날에_전체_계획량과_딜레이를_배정한다() {
        TotalQuantity totalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );

        List<WeeklyQuantity> weeklyQuantities = totalQuantity.getWeeklyQuantities();

        assertDailyQuantityAmount(weeklyQuantities.getFirst().getDailyQuantities().getFirst(), TOTAL_AMOUNT);
        weeklyQuantities.getFirst()
                .getDailyQuantities()
                .stream()
                .skip(1)
                .forEach(dailyQuantity -> assertDailyQuantityAmount(dailyQuantity, 0));
        weeklyQuantities.stream()
                .skip(1)
                .flatMap(weeklyQuantity -> weeklyQuantity.getDailyQuantities().stream())
                .forEach(dailyQuantity -> assertDailyQuantityAmount(dailyQuantity, 0));
    }

    private void assertDailyQuantityAmount(DailyQuantity dailyQuantity, int expectedAmount) {
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        assertThat(dailyQuantityAmount.quantityAmount().value()).isEqualTo(expectedAmount);
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isZero();
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isEqualTo(expectedAmount);
    }

}
