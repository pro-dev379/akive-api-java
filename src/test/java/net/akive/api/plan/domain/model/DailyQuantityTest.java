package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityMemo;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createSubject;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createTotalQuantityAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyQuantityTest {

    @Test
    void 저장된_일일_계획량을_복원한다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(40, 10, 30);

        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                dailyQuantityAmount
        );

        assertThat(dailyQuantity.getDailyQuantityId()).isEqualTo(FIRST_DAILY_QUANTITY_ID);
        assertThat(dailyQuantity.getDate()).isEqualTo(FIRST_DATE);
        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(dailyQuantityAmount);
    }

    @Test
    void 일일_계획_달성량을_늘리면_딜레이량을_줄인다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        dailyQuantity.updateCompletedAmount(new QuantityAmount(30));

        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(40, 30, 10));
    }

    @Test
    void 일일_계획_달성량을_줄이면_딜레이량을_늘린다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        dailyQuantity.updateCompletedAmount(new QuantityAmount(5));

        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(40, 5, 35));
    }

    @Test
    void 일일_계획_달성량을_모두_취소한다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        dailyQuantity.updateCompletedAmount(QuantityAmount.ZERO);

        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(40, 0, 40));
    }

    @Test
    void 일일_계획_달성량이_이전과_같다면_수정하지_않는다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        dailyQuantity.updateCompletedAmount(new QuantityAmount(10));

        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(40, 10, 30));
    }

    @Test
    void 일일_계획_달성량_증가분이_딜레이량보다_많으면_수정할_수_없다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyQuantity.updateCompletedAmount(new QuantityAmount(50)))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);
    }

    @Test
    void 일일_딜레이를_다른_일일_계획량으로_옮긴다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );
        DailyQuantity receivingDailyQuantity = DailyQuantity.reconstruct(
                SECOND_DAILY_QUANTITY_ID,
                SECOND_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        dailyQuantity.delayTo(receivingDailyQuantity, new QuantityAmount(20));

        assertThat(dailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(20, 10, 10));
        assertThat(receivingDailyQuantity.getDailyQuantityAmount())
                .isEqualTo(DailyQuantityAmount.of(60, 10, 50));
    }

    @Test
    void 현재_일일_딜레이보다_많은_수량을_다른_일일_계획량으로_옮길_수_없다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );
        DailyQuantity receivingDailyQuantity = DailyQuantity.reconstruct(
                SECOND_DAILY_QUANTITY_ID,
                SECOND_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyQuantity.delayTo(
                        receivingDailyQuantity,
                        new QuantityAmount(40)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT);
    }

    @Test
    void 일일_계획량은_늘릴_수_없다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyQuantity.updateAmount(
                        new QuantityAmount(50),
                        null
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_AMOUNT_INCREASE_NOT_ALLOWED);
    }

    @Test
    void 일일_계획량을_달성량보다_적게_수정할_수_없다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> dailyQuantity.updateAmount(
                        new QuantityAmount(5),
                        null
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
    }

    @Test
    void 일일_계획량이_이전과_같으면_메모만_수정한다() {
        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                FIRST_DAILY_QUANTITY_ID,
                FIRST_DATE,
                DailyQuantityAmount.of(40, 10, 30)
        );
        DailyQuantityAmount currentDailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        dailyQuantity.updateAmount(currentDailyQuantityAmount.quantityAmount(), new QuantityMemo(TARGET_DAILY_MEMO));


        assertThat(dailyQuantity.getDailyQuantityAmount().quantityAmount())
                .isEqualTo(currentDailyQuantityAmount.quantityAmount());
        assertThat(dailyQuantity.getDailyQuantityAmount().completedQuantityAmount())
                .isEqualTo(currentDailyQuantityAmount.completedQuantityAmount());
        assertThat(dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount())
                .isEqualTo(currentDailyQuantityAmount.delayedQuantityAmount());
        assertThat(dailyQuantity.getDailyQuantityAmount().quantityMemo().value()).isEqualTo(TARGET_DAILY_MEMO);
    }

    @Test
    void 같은_식별자를_가진_일일_계획량은_같게_취급한다() {
        DailyQuantity dailyQuantity = createReconstructedDailyQuantity(FIRST_DAILY_QUANTITY_ID);
        DailyQuantity sameDailyQuantity = createReconstructedDailyQuantity(FIRST_DAILY_QUANTITY_ID);

        assertThat(dailyQuantity).isEqualTo(sameDailyQuantity);
        assertThat(dailyQuantity.hashCode()).isEqualTo(sameDailyQuantity.hashCode());
    }

    @Test
    void 식별자가_다른_일일_계획량은_다르게_취급한다() {
        DailyQuantity dailyQuantity = createReconstructedDailyQuantity(FIRST_DAILY_QUANTITY_ID);
        DailyQuantity anotherDailyQuantity = createReconstructedDailyQuantity(SECOND_DAILY_QUANTITY_ID);

        assertThat(dailyQuantity).isNotEqualTo(anotherDailyQuantity);
    }

    @Test
    void 식별자가_없는_일일_계획량끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
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

        assertThat(totalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst())
                .isNotEqualTo(anotherTotalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst());
    }
}
