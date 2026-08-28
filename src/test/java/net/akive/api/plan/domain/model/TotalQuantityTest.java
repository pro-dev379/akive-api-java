package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.domain.value.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;
import static net.akive.api.common.exception.business.ConflictError.WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;
import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.PlanAssertions.assertWeeklyQuantityAmounts;
import static net.akive.api.plan.domain.PlanFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
    void 저장된_세션_총량을_복원한다() {
        Long totalQuantityId = 1L;
        SessionalPlan sessionalPlan = createSessionalPlan();
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = createTotalQuantityAmount();
        List<WeeklyQuantity> weeklyQuantities = List.of(
                createWeeklyQuantity(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 4),
                        TOTAL_AMOUNT
                )
        );

        TotalQuantity totalQuantity = TotalQuantity.reconstruct(
                totalQuantityId,
                sessionalPlan,
                subject,
                totalQuantityAmount,
                weeklyQuantities
        );

        assertThat(totalQuantity.getTotalQuantityId()).isEqualTo(totalQuantityId);
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantity.getSubject()).isEqualTo(subject);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThat(totalQuantity.getWeeklyQuantities()).containsExactlyElementsOf(weeklyQuantities);
    }

    @Test
    void 주간_계획량_합계가_세션_총량과_다르면_복원할_수_없다() {
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, QUANTITY_UNIT, null);
        List<WeeklyQuantity> weeklyQuantities = List.of(
                createWeeklyQuantity(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 4),
                        60
                )
        );

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> TotalQuantity.reconstruct(
                        1L,
                        createSessionalPlan(),
                        createSubject(),
                        totalQuantityAmount,
                        weeklyQuantities
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(TOTAL_QUANTITY_AMOUNT_MISMATCH);
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

    @Test
    void 세션_총량을_늘리면_첫_주의_첫날_계획량을_늘린다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        TotalQuantityAmount newTotalQuantityAmount = TotalQuantityAmount.of(130, QUANTITY_UNIT, null);

        totalQuantity.update(createSubject(), newTotalQuantityAmount);

        assertWeeklyQuantityAmounts(totalQuantity, 60, 40, 30);
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst(),
                60
        );
    }

    @Test
    void 세션_총량을_줄이면_앞_주차부터_계획량을_줄인다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        TotalQuantityAmount newTotalQuantityAmount = TotalQuantityAmount.of(50, QUANTITY_UNIT, null);

        totalQuantity.update(createSubject(), newTotalQuantityAmount);

        assertWeeklyQuantityAmounts(totalQuantity, 0, 20, 30);
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().get(0).getDailyQuantities().getFirst(),
                0
        );
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().get(1).getDailyQuantities().getFirst(),
                20
        );
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().get(2).getDailyQuantities().getFirst(),
                30
        );
    }

    @Test
    void 세션_총량을_현재_완료량보다_적게_수정할_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedAmount(100, 30);
        TotalQuantityAmount newTotalQuantityAmount = TotalQuantityAmount.of(20, QUANTITY_UNIT, null);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantity.update(createSubject(), newTotalQuantityAmount))
                .extracting(ConflictException::getError)
                .isEqualTo(TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);
    }

    @Test
    void 주간_계획량을_줄이면_다음_주간_계획량을_늘린다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmount reducedWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, "첫 주 조정");

        totalQuantity.reduceWeeklyQuantity(1L, reducedWeeklyQuantityAmount);

        assertWeeklyQuantityAmounts(totalQuantity, 20, 50, 30);
        assertThat(totalQuantity.getWeeklyQuantities().getFirst().getWeeklyQuantityAmount())
                .isEqualTo(reducedWeeklyQuantityAmount);
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst(),
                20
        );
        assertDailyQuantityAmount(
                totalQuantity.getWeeklyQuantities().get(1).getDailyQuantities().getFirst(),
                50
        );
    }

    @Test
    void 주간_계획량을_수정하지_않으면_메모만_수정한다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmount reducedWeeklyQuantityAmount = WeeklyQuantityAmount.of(30, "메모 수정");

        totalQuantity.reduceWeeklyQuantity(1L, reducedWeeklyQuantityAmount);

        assertWeeklyQuantityAmounts(totalQuantity, 30, 40, 30);
        assertThat(totalQuantity.getWeeklyQuantities()
                .getFirst()
                .getWeeklyQuantityAmount()
                .quantityMemo()
                .value())
                .isEqualTo("메모 수정");
    }

    @Test
    void 마지막_주간_계획량은_줄일_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmount reducedWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantity.reduceWeeklyQuantity(3L, reducedWeeklyQuantityAmount))
                .extracting(BadRequestException::getError)
                .isEqualTo(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);
    }

    @Test
    void 주간_계획량을_늘릴_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmount increasedWeeklyQuantityAmount = WeeklyQuantityAmount.of(40, null);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> totalQuantity.reduceWeeklyQuantity(1L, increasedWeeklyQuantityAmount))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED);
    }

    @Test
    void 주간_계획량을_주간_계획_완료량보다_적게_줄일_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedFirstWeek(100, 30);
        WeeklyQuantityAmount reducedWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> totalQuantity.reduceWeeklyQuantity(1L, reducedWeeklyQuantityAmount))
                .extracting(ConflictException::getError)
                .isEqualTo(WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);
    }

    @Test
    void 존재하지_않는_주간_계획량은_줄일_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(30, 40, 30);
        WeeklyQuantityAmount reducedWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, null);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> totalQuantity.reduceWeeklyQuantity(999L, reducedWeeklyQuantityAmount))
                .extracting(NotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    private void assertDailyQuantityAmount(DailyQuantity dailyQuantity, int expectedAmount) {
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        assertThat(dailyQuantityAmount.quantityAmount().value()).isEqualTo(expectedAmount);
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isZero();
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isEqualTo(expectedAmount);
    }

    private TotalQuantity createTotalQuantityWithCompletedAmount(int quantityAmount, int completedQuantityAmount) {
        return TotalQuantity.reconstruct(
                1L,
                createSessionalPlan(),
                createSubject(),
                TotalQuantityAmount.of(quantityAmount, QUANTITY_UNIT, null),
                List.of(WeeklyQuantity.reconstruct(
                        1L,
                        new WeekPeriod(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 4)
                        ),
                        WeeklyQuantityAmount.of(quantityAmount, null),
                        List.of(DailyQuantity.reconstruct(
                                1L,
                                LocalDate.of(2026, 1, 1),
                                DailyQuantityAmount.of(
                                        quantityAmount,
                                        completedQuantityAmount,
                                        quantityAmount - completedQuantityAmount,
                                        null
                                )
                        ))
                ))
        );
    }

    private TotalQuantity createTotalQuantityWithCompletedFirstWeek(int quantityAmount, int completedQuantityAmount) {
        return TotalQuantity.reconstruct(
                1L,
                createSessionalPlan(),
                createSubject(),
                TotalQuantityAmount.of(quantityAmount, QUANTITY_UNIT, null),
                List.of(
                        createWeeklyQuantityWithCompletedAmount(
                                1L,
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 4),
                                quantityAmount,
                                completedQuantityAmount
                        ),
                        createWeeklyQuantity(
                                2L,
                                LocalDate.of(2026, 1, 5),
                                LocalDate.of(2026, 1, 11),
                                0
                        )
                )
        );
    }

    private WeeklyQuantity createWeeklyQuantityWithCompletedAmount(
            Long weeklyQuantityId,
            LocalDate startOn,
            LocalDate endOn,
            int quantityAmount,
            int completedQuantityAmount
    ) {
        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                new WeekPeriod(startOn, endOn),
                WeeklyQuantityAmount.of(quantityAmount, null),
                List.of(DailyQuantity.reconstruct(
                        weeklyQuantityId,
                        startOn,
                        DailyQuantityAmount.of(
                                quantityAmount,
                                completedQuantityAmount,
                                quantityAmount - completedQuantityAmount,
                                null
                        )
                ))
        );
    }

}
