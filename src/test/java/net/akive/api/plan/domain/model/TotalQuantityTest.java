package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.domain.value.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.WEEKLY_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.assertion.TotalQuantityAssert.assertThatTotalQuantity;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.FIRST_DAILY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.SECOND_DAILY_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createReconstructedSessionalPlan;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.*;
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
        SessionalPlan sessionalPlan = createSessionalPlan();
        Subject subject = createSubject();
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(TOTAL_AMOUNT, QUANTITY_UNIT);
        List<WeeklyQuantity> weeklyQuantities = List.of(
                createWeeklyQuantity(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEK_PERIOD.startOn(),
                        FIRST_WEEK_PERIOD.endOn(),
                        TOTAL_AMOUNT
                )
        );

        TotalQuantity totalQuantity = TotalQuantity.reconstruct(
                FIRST_TOTAL_QUANTITY_ID,
                sessionalPlan,
                subject,
                totalQuantityAmount,
                weeklyQuantities
        );

        assertThat(totalQuantity.getTotalQuantityId()).isEqualTo(FIRST_TOTAL_QUANTITY_ID);
        assertThat(totalQuantity.getSessionalPlan()).isEqualTo(sessionalPlan);
        assertThat(totalQuantity.getSubject()).isEqualTo(subject);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(totalQuantityAmount);
        assertThat(totalQuantity.getWeeklyQuantities()).containsExactlyElementsOf(weeklyQuantities);
    }

    @Test
    void 주간_계획량_합계가_세션_총량과_다르면_복원할_수_없다() {
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(TOTAL_AMOUNT, QUANTITY_UNIT);
        List<WeeklyQuantity> weeklyQuantities = List.of(
                createWeeklyQuantity(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEK_PERIOD.startOn(),
                        FIRST_WEEK_PERIOD.endOn(),
                        60
                )
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> TotalQuantity.reconstruct(
                        FIRST_TOTAL_QUANTITY_ID,
                        createSessionalPlan(),
                        createSubject(),
                        totalQuantityAmount,
                        weeklyQuantities
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(TOTAL_AMOUNT_MISMATCH);
    }

    @Test
    void 세션_기간의_주차_수만큼_주간_계획량을_생성한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();

        TotalQuantity totalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity.getWeeklyQuantities()).hasSize(3);
        assertThat(totalQuantity.getWeeklyQuantities())
                .extracting(WeeklyQuantity::getWeekPeriod)
                .containsExactly(
                        FIRST_WEEK_PERIOD,
                        SECOND_WEEK_PERIOD,
                        THIRD_WEEK_PERIOD
                );
    }

    @Test
    void 세션_총량을_생성하면_첫_주에_전체_계획량을_배정한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        TotalQuantity totalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(TOTAL_AMOUNT, QUANTITY_UNIT)
        );

        WeeklyQuantity firstWeeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();

        assertThat(firstWeeklyQuantity.getWeekPeriod()).isEqualTo(FIRST_WEEK_PERIOD);
        assertThat(firstWeeklyQuantity.getWeeklyQuantityAmount()
                .quantityAmount()
                .value())
                .isEqualTo(TOTAL_AMOUNT);
        assertThat(totalQuantity.getWeeklyQuantities()
                .stream()
                .skip(1))
                .extracting(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount()
                        .quantityAmount()
                        .value())
                .containsOnly(0);
    }

    @Test
    void 세션_총량을_생성하면_첫_주의_첫날에_전체_계획량과_딜레이를_배정한다() {
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan();
        TotalQuantity totalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                TotalQuantityAmount.of(TOTAL_AMOUNT, QUANTITY_UNIT)
        );

        List<WeeklyQuantity> weeklyQuantities = totalQuantity.getWeeklyQuantities();

        assertThat(weeklyQuantities.getFirst()
                .getDailyQuantities()
                .getFirst()
                .getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(TOTAL_AMOUNT, 0, TOTAL_AMOUNT));
        weeklyQuantities.getFirst()
                .getDailyQuantities()
                .stream()
                .skip(1)
                .forEach(dailyQuantity -> assertThat(dailyQuantity.getDailyQuantityAmount())
                        .isEqualTo(DailyQuantityAmount.of(0, 0, 0)));
        weeklyQuantities.stream()
                .skip(1)
                .flatMap(weeklyQuantity -> weeklyQuantity.getDailyQuantities().stream())
                .forEach(dailyQuantity -> assertThat(dailyQuantity.getDailyQuantityAmount())
                        .isEqualTo(DailyQuantityAmount.of(0, 0, 0)));
    }

    @Test
    void 세션_총량을_늘리면_첫_주의_계획량을_늘린다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(130, QUANTITY_UNIT);

        totalQuantity.update(createSubject(), targetTotalQuantityAmount);

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(70, 40, 20);
    }

    @Test
    void 세션_총량을_줄이면_앞_주차부터_계획량을_줄인다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(50, QUANTITY_UNIT);

        totalQuantity.update(createSubject(), targetTotalQuantityAmount);

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(0, 30, 20);
    }

    @Test
    void 세션_총량이_이전과_같으면_과목만_수정한다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);
        TotalQuantityAmount currentTotalQuantityAmount = totalQuantity.getTotalQuantityAmount();

        totalQuantity.update(TARGET_SUBJECT, currentTotalQuantityAmount);

        assertThat(totalQuantity.getSubject()).isEqualTo(TARGET_SUBJECT);
        assertThat(totalQuantity.getTotalQuantityAmount()).isEqualTo(currentTotalQuantityAmount);
        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(40, 40, 20);
    }

    @Test
    void 세션_총량이_이전과_같으면_메모만_수정한다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);
        Subject currentSubject = totalQuantity.getSubject();
        TotalQuantityAmount currentTotalQuantityAmount = totalQuantity.getTotalQuantityAmount();
        TotalQuantityAmount targetTotalQuantityAmount = new TotalQuantityAmount(
                currentTotalQuantityAmount.quantityAmount(),
                currentTotalQuantityAmount.quantityUnit(),
                new QuantityMemo(TARGET_TOTAL_MEMO)
        );

        totalQuantity.update(currentSubject, targetTotalQuantityAmount);

        assertThat(totalQuantity.getSubject()).isEqualTo(currentSubject);
        assertThat(totalQuantity.getTotalQuantityAmount().quantityAmount())
                .isEqualTo(currentTotalQuantityAmount.quantityAmount());
        assertThat(totalQuantity.getTotalQuantityAmount().quantityUnit())
                .isEqualTo(currentTotalQuantityAmount.quantityUnit());
        assertThat(totalQuantity.getTotalQuantityAmount().quantityMemo().value())
                .isEqualTo(TARGET_TOTAL_MEMO);
        assertThat(totalQuantity.getTotalQuantityAmount().quantityAmount())
                .isEqualTo(targetTotalQuantityAmount.quantityAmount());
        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(40, 40, 20);
    }

    @Test
    void 세션_총량을_현재_달성량보다_적게_수정할_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithCompletedAmount(100, 30);
        TotalQuantityAmount targetTotalQuantityAmount = TotalQuantityAmount.of(20, QUANTITY_UNIT);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantity.update(createSubject(), targetTotalQuantityAmount))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
    }

    @Test
    void 주간_계획량을_줄이면_다음_주간_계획량을_늘린다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20, "첫 주 조정");

        totalQuantity.updateWeeklyQuantityAmount(FIRST_WEEKLY_QUANTITY_ID, targetWeeklyQuantityAmount);

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(20, 60, 20);
        assertThat(totalQuantity.getWeeklyQuantities().getFirst().getWeeklyQuantityAmount())
                .isEqualTo(targetWeeklyQuantityAmount);
    }

    @Test
    void 마지막_주간_계획량은_줄일_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantity.updateWeeklyQuantityAmount(
                        THIRD_WEEKLY_QUANTITY_ID,
                        WeeklyQuantityAmount.of(10)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);
    }

    @Test
    void 존재하지_않는_주간_계획량은_줄일_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.updateWeeklyQuantityAmount(
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        TARGET_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 주간_딜레이를_다른_주로_옮긴다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);

        totalQuantity.delayWeeklyAmountTo(
                FIRST_WEEKLY_QUANTITY_ID,
                THIRD_WEEKLY_QUANTITY_ID,
                new QuantityAmount(10)
        );

        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(30, 40, 30);
    }

    @Test
    void 같은_주에_딜레이를_옮길_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantity.delayWeeklyAmountTo(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEKLY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_DELAY_RECEIVER_SAME_AS_SOURCE);
    }

    @Test
    void 주간_계획량이_존재하지_않으면_딜레이를_옮길_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(40, 40, 20);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.delayWeeklyAmountTo(
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        SECOND_WEEKLY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.delayWeeklyAmountTo(
                        FIRST_WEEKLY_QUANTITY_ID,
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 일일_딜레이를_같은_주의_다른_날로_옮긴다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        totalQuantity.delayDailyAmountTo(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID,
                SECOND_DAILY_QUANTITY_ID,
                new QuantityAmount(10)
        );

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertThat(weeklyQuantity.getDailyQuantities()
                .getFirst()
                .getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(30, 0, 30));
        assertThat(weeklyQuantity.getDailyQuantities()
                .get(1)
                .getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(50, 0, 50));
        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(80, 0, 0);
    }

    @Test
    void 같은_날로_딜레이를_옮길_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> totalQuantity.delayDailyAmountTo(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_DAILY_QUANTITY_ID,
                        FIRST_DAILY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_DELAY_RECEIVER_SAME_AS_SOURCE);
    }

    @Test
    void 존재하지_않는_주간_계획량에서는_일일_딜레이를_옮길_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.delayDailyAmountTo(
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        FIRST_DAILY_QUANTITY_ID,
                        SECOND_DAILY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_주간_계획량에서는_일일_계획량을_수정할_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.updateDailyAmount(
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        FIRST_DAILY_QUANTITY_ID,
                        new QuantityAmount(20),
                        null
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 일일_계획_달성량을_수정한다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 10, 30),
                DailyQuantityAmount.of(40, 0, 40)
        );

        totalQuantity.updateDailyCompletedAmount(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_DAILY_QUANTITY_ID,
                new QuantityAmount(30)
        );

        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().getFirst();
        assertThat(weeklyQuantity.getDailyQuantities()
                .getFirst()
                .getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(40, 30, 10));
        assertThatTotalQuantity(totalQuantity).hasWeeklyAmounts(80, 0, 0);
    }

    @Test
    void 존재하지_않는_주간_계획량에서는_일일_계획_달성량을_수정할_수_없다() {
        TotalQuantity totalQuantity = createTotalQuantityWithDailyAmounts(
                DailyQuantityAmount.of(40, 10, 30),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> totalQuantity.updateDailyCompletedAmount(
                        UNKNOWN_WEEKLY_QUANTITY_ID,
                        FIRST_DAILY_QUANTITY_ID,
                        new QuantityAmount(20)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(WEEKLY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 같은_식별자를_가진_세션_총량은_같게_취급한다() {
        TotalQuantity totalQuantity = createTotalQuantityWithWeeklyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                createSessionalPlan(),
                30, 40, 30
        );
        TotalQuantity sameTotalQuantity = createTotalQuantityWithWeeklyAmounts(
                FIRST_TOTAL_QUANTITY_ID,
                createSessionalPlan(),
                20, 50, 30
        );

        assertThat(totalQuantity).isEqualTo(sameTotalQuantity);
        assertThat(totalQuantity.hashCode()).isEqualTo(sameTotalQuantity.hashCode());
    }

    @Test
    void 식별자가_다른_세션_총량은_다르게_취급한다() {
        TotalQuantity totalQuantity =
                createTotalQuantityWithWeeklyAmounts(FIRST_TOTAL_QUANTITY_ID, createSessionalPlan(), 40, 40, 20);
        TotalQuantity anotherTotalQuantity =
                createTotalQuantityWithWeeklyAmounts(SECOND_TOTAL_QUANTITY_ID, createSessionalPlan(), 40, 40, 20);

        assertThat(totalQuantity).isNotEqualTo(anotherTotalQuantity);
    }

    @Test
    void 식별자가_없는_세션_총량끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        SessionalPlan sessionalPlan = createSessionalPlan();
        TotalQuantity totalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount()
        );
        TotalQuantity anotherTotalQuantity = TotalQuantity.create(
                sessionalPlan,
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity).isNotEqualTo(anotherTotalQuantity);
    }
}
