package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityMemo;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceNotFoundError.DAILY_QUANTITY_NOT_FOUND;
import static net.akive.api.plan.domain.assertion.WeeklyQuantityAssert.assertThatWeeklyQuantity;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSION_PERIOD;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createSubject;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createTotalQuantityAmount;
import static net.akive.api.plan.domain.fixture.WeeklyQuantityFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityTest {

    @Test
    void 저장된_주간_계획량을_복원한다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);
        List<DailyQuantity> dailyQuantities = List.of(DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(100, 0, 100)
        ));

        WeeklyQuantity weeklyQuantity = WeeklyQuantity.reconstruct(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_WEEK_PERIOD,
                weeklyQuantityAmount,
                dailyQuantities
        );

        assertThat(weeklyQuantity.getWeeklyQuantityId()).isEqualTo(FIRST_WEEKLY_QUANTITY_ID);
        assertThat(weeklyQuantity.getWeekPeriod()).isEqualTo(FIRST_WEEK_PERIOD);
        assertThatWeeklyQuantity(weeklyQuantity).hasAmount(weeklyQuantityAmount);
        assertThat(weeklyQuantity.getDailyQuantities()).containsExactlyElementsOf(dailyQuantities);
    }

    @Test
    void 일일_계획량_합계가_주간_계획량과_다르면_저장된_주간_계획량을_복원할_수_없다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        FIRST_DAILY_QUANTITY_ID,
                        FIRST_DATE,
                        DailyQuantityAmount.of(60, 0, 60)
                )
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEK_PERIOD,
                        weeklyQuantityAmount,
                        dailyQuantities
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_AMOUNT_MISMATCH);
    }

    @Test
    void 일일_계획량_날짜가_주간_기간을_벗어나면_저장된_주간_계획량을_복원할_수_없다() {
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        FIRST_DAILY_QUANTITY_ID,
                        LocalDate.of(2099, 1, 1),
                        DailyQuantityAmount.of(100, 0, 100)
                )
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        FIRST_WEEKLY_QUANTITY_ID,
                        FIRST_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(100),
                        dailyQuantities
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
    }

    @Test
    void 주간_계획량을_생성하면_주차_기간의_날짜별로_일일_계획량을_생성한다() {
        WeeklyQuantity weeklyQuantity = WeeklyQuantity.create(
                createTotalQuantityAmount(),
                SESSION_PERIOD,
                SECOND_WEEK_PERIOD,
                WeeklyQuantityAmount.of(0),
                false
        );

        assertThat(weeklyQuantity.getDailyQuantities())
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
    }

    @Test
    void 주간_계획량을_줄이면_일일_계획량을_앞에서부터_줄인다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(10, 0, 10),
                DailyQuantityAmount.of(20, 0, 20)
        );
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(15);

        weeklyQuantity.updateQuantityAmount(targetWeeklyQuantityAmount);

        assertThatWeeklyQuantity(weeklyQuantity)
                .hasAmount(targetWeeklyQuantityAmount)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(0, 0, 0),
                        DailyQuantityAmount.of(15, 0, 15)
                );
    }

    @Test
    void 주간_계획량이_이전과_같으면_메모만_수정한다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );
        WeeklyQuantityAmount currentWeeklyQuantityAmount = weeklyQuantity.getWeeklyQuantityAmount();
        WeeklyQuantityAmount targetWeeklyQuantityAmount = new WeeklyQuantityAmount(
                currentWeeklyQuantityAmount.quantityAmount(),
                new QuantityMemo(TARGET_WEEKLY_MEMO)
        );

        weeklyQuantity.updateQuantityAmount(targetWeeklyQuantityAmount);

        assertThat(targetWeeklyQuantityAmount.quantityAmount())
                .isEqualTo(currentWeeklyQuantityAmount.quantityAmount());
        assertThat(targetWeeklyQuantityAmount.quantityMemo().value())
                .isEqualTo(TARGET_WEEKLY_MEMO);
        assertThatWeeklyQuantity(weeklyQuantity)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(40, 0, 40),
                        DailyQuantityAmount.of(40, 0, 40)
                );
    }

    @Test
    void 주간_계획량은_늘릴_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(90);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> weeklyQuantity.updateQuantityAmount(targetWeeklyQuantityAmount))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED);
    }

    @Test
    void 주간_계획량을_주간_계획_달성량보다_적게_줄일_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 30, 10)
        );
        WeeklyQuantityAmount targetWeeklyQuantityAmount = WeeklyQuantityAmount.of(20);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> weeklyQuantity.updateQuantityAmount(targetWeeklyQuantityAmount))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
    }

    @Test
    void 주간_계획량을_늘리면_첫_일일_계획량을_늘린다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        weeklyQuantity.increaseBy(new QuantityAmount(20));

        assertThatWeeklyQuantity(weeklyQuantity)
                .hasAmount(100)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(60, 0, 60),
                        DailyQuantityAmount.of(40, 0, 40)
                );
    }

    @Test
    void 주간_딜레이를_다른_주간_계획량으로_옮기면_일일_계획량도_함께_조정한다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );
        WeeklyQuantity receivingWeeklyQuantity = createWeeklyQuantity(
                SECOND_WEEKLY_QUANTITY_ID,
                SECOND_WEEK_PERIOD.startOn(),
                SECOND_WEEK_PERIOD.endOn(),
                DailyQuantityAmount.of(0, 0, 0)
        );

        weeklyQuantity.delayAmountTo(receivingWeeklyQuantity, new QuantityAmount(20));

        assertThatWeeklyQuantity(weeklyQuantity)
                .hasAmount(60)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(20, 0, 20),
                        DailyQuantityAmount.of(40, 0, 40)
                );
        assertThatWeeklyQuantity(receivingWeeklyQuantity)
                .hasAmount(20)
                .hasDailyAmounts(DailyQuantityAmount.of(20, 0, 20));
    }

    @Test
    void 현재_딜레이보다_많은_수량을_다른_주간_계획량으로_옮길_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 10, 30)
        );
        WeeklyQuantity receivingWeeklyQuantity = createReconstructedWeeklyQuantity(SECOND_WEEKLY_QUANTITY_ID);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> weeklyQuantity.delayAmountTo(
                        receivingWeeklyQuantity,
                        new QuantityAmount(40)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT);
    }

    @Test
    void 일일_계획량을_줄이면_다음_일일_계획량을_늘린다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        weeklyQuantity.updateDailyAmount(
                FIRST_DAILY_QUANTITY_ID,
                new QuantityAmount(20),
                new QuantityMemo(TARGET_DAILY_MEMO)
        );

        assertThatWeeklyQuantity(weeklyQuantity)
                .hasAmount(80)
                .hasDailyAmounts(
                        DailyQuantityAmount.of(20, 0, 20, TARGET_DAILY_MEMO),
                        DailyQuantityAmount.of(60, 0, 60)
                );
    }

    @Test
    void 마지막_일일_계획량은_줄일_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> weeklyQuantity.updateDailyAmount(
                        SECOND_DAILY_QUANTITY_ID,
                        new QuantityAmount(30),
                        null
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED);
    }

    @Test
    void 존재하지_않는_일일_계획량으로_딜레이를_옮길_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyQuantity.delayDailyAmountTo(
                        UNKNOWN_DAILY_QUANTITY_ID,
                        SECOND_DAILY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(DAILY_QUANTITY_NOT_FOUND);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyQuantity.delayDailyAmountTo(
                        FIRST_DAILY_QUANTITY_ID,
                        UNKNOWN_DAILY_QUANTITY_ID,
                        new QuantityAmount(10)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(DAILY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_일일_계획량은_수정할_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyQuantity.updateDailyAmount(
                        UNKNOWN_DAILY_QUANTITY_ID,
                        new QuantityAmount(20),
                        null
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(DAILY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_일일_계획량은_완료량을_수정할_수_없다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(
                DailyQuantityAmount.of(40, 0, 40),
                DailyQuantityAmount.of(40, 0, 40)
        );

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> weeklyQuantity.updateDailyCompletedAmount(
                        UNKNOWN_DAILY_QUANTITY_ID,
                        new QuantityAmount(20)
                ))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(DAILY_QUANTITY_NOT_FOUND);
    }

    @Test
    void 같은_식별자를_가진_주간_계획량은_같게_취급한다() {
        WeeklyQuantity weeklyQuantity = createReconstructedWeeklyQuantity(FIRST_WEEKLY_QUANTITY_ID);
        WeeklyQuantity sameWeeklyQuantity = createReconstructedWeeklyQuantity(FIRST_WEEKLY_QUANTITY_ID);

        assertThat(weeklyQuantity).isEqualTo(sameWeeklyQuantity);
        assertThat(weeklyQuantity.hashCode()).isEqualTo(sameWeeklyQuantity.hashCode());
    }

    @Test
    void 식별자가_다른_주간_계획량은_다르게_취급한다() {
        WeeklyQuantity weeklyQuantity = createReconstructedWeeklyQuantity(FIRST_WEEKLY_QUANTITY_ID);
        WeeklyQuantity anotherWeeklyQuantity = createReconstructedWeeklyQuantity(SECOND_WEEKLY_QUANTITY_ID);

        assertThat(weeklyQuantity).isNotEqualTo(anotherWeeklyQuantity);
    }

    @Test
    void 식별자가_없는_주간_계획량끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        TotalQuantity totalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );
        TotalQuantity anotherTotalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity.getWeeklyQuantities().getFirst())
                .isNotEqualTo(anotherTotalQuantity.getWeeklyQuantities().getFirst());
    }
}
