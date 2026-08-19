package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.*;

class TotalQuantityPlanFactoryTest {

    private final TotalQuantityPlanFactory totalQuantityPlanFactory = new TotalQuantityPlanFactory();

    @Test
    void 세션_기간에_맞춰_세션_총량_계획을_생성한다() {
        // given
        Subject subject = new Subject("수학", "과목");
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, "문제", null);
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                2L,
                "세션",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                subject,
                totalQuantityAmount
        );

        // then
        assertThat(totalQuantityPlan.totalQuantity().getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantityPlan.totalQuantity().getSubject()).isEqualTo(subject);
        assertThat(totalQuantityPlan.totalQuantity().getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThat(totalQuantityPlan.weeklyQuantities()).hasSize(2);
    }

    @Test
    void 세션_기간을_주차별_기간으로_나누어_주간_계획을_생성한다() {
        // given
        SessionalPlan sessionalPlan = createSessionalPlan(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );

        // then
        assertThat(totalQuantityPlan.weeklyQuantities())
                .extracting(
                        weeklyQuantityPlan -> weeklyQuantityPlan.weeklyQuantity().getWeekPeriod().startOn(),
                        weeklyQuantityPlan -> weeklyQuantityPlan.weeklyQuantity().getWeekPeriod().endOn()
                )
                .containsExactly(
                        tuple(LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 12)),
                        tuple(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
                );
    }

    @Test
    void 전체_계획량은_첫_번째_주간_계획량에만_배정한다() {
        // given
        SessionalPlan sessionalPlan = createSessionalPlan(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );

        // then
        assertThat(totalQuantityPlan.weeklyQuantities())
                .extracting(weeklyQuantityPlan -> weeklyQuantityPlan.weeklyQuantity()
                        .getWeeklyQuantityAmount().quantityAmount().value())
                .containsExactly(100, 0);
    }

    @Test
    void 각_주간_계획의_기간에_맞춰_하루_계획량을_생성한다() {
        // given
        SessionalPlan sessionalPlan = createSessionalPlan(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );

        // then
        List<WeeklyQuantityPlan> weeklyQuantityPlans = totalQuantityPlan.weeklyQuantities();
        assertThat(weeklyQuantityPlans.getFirst().dailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 9),
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 11),
                        LocalDate.of(2026, 7, 12)
                );
        assertThat(weeklyQuantityPlans.get(1).dailyQuantities())
                .extracting(DailyQuantity::getDate)
                .containsExactly(
                        LocalDate.of(2026, 7, 13),
                        LocalDate.of(2026, 7, 14)
                );
    }

    @Test
    void 첫_번째_주간_계획의_첫_번째_하루_계획량에만_전체_계획량을_배정한다() {
        // given
        SessionalPlan sessionalPlan = createSessionalPlan(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );

        // then
        assertThat(totalQuantityPlan.weeklyQuantities().getFirst().dailyQuantities())
                .extracting(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount().value())
                .containsExactly(100, 0, 0, 0, 0, 0);
        assertThat(totalQuantityPlan.weeklyQuantities().get(1).dailyQuantities())
                .extracting(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount().value())
                .containsExactly(0, 0);
    }

    @Test
    void 하루_계획량의_완료량은_0이고_딜레이량은_계획량과_같다() {
        // given
        SessionalPlan sessionalPlan = createSessionalPlan(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 8)
        );

        // when
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                new Subject("수학", "과목"),
                TotalQuantityAmount.of(100, "문제", null)
        );

        // then
        assertThat(totalQuantityPlan.weeklyQuantities().getFirst().dailyQuantities())
                .extracting(
                        dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount().value(),
                        dailyQuantity -> dailyQuantity.getDailyQuantityAmount().completedQuantityAmount().value(),
                        dailyQuantity -> dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount().value()
                )
                .containsExactly(
                        tuple(100, 0, 100),
                        tuple(0, 0, 0)
                );
    }

    @Test
    void 세셔널_플랜은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityPlanFactory.create(
                        null,
                        new Subject("수학", "과목"),
                        TotalQuantityAmount.of(100, "문제", null)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 과목은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityPlanFactory.create(
                        createSessionalPlan(LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 14)),
                        null,
                        TotalQuantityAmount.of(100, "문제", null)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 세션_총량은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantityPlanFactory.create(
                        createSessionalPlan(LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 14)),
                        new Subject("수학", "과목"),
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    private SessionalPlan createSessionalPlan(LocalDate startOn, LocalDate endOn) {
        return SessionalPlan.create(
                1L,
                2L,
                "세션",
                new SessionPeriod(startOn, endOn)
        );
    }
}
