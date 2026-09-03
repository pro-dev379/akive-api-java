package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyQuantityAmountTest {

    @Test
    void 일일_계획량을_생성한다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70, "메모");

        assertThat(dailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(dailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(30));
        assertThat(dailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(70));
        assertThat(dailyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 메모가_없으면_빈_메모로_일일_계획량을_생성한다() {
        DailyQuantityAmount dailyQuantityAmount = new DailyQuantityAmount(
                new QuantityAmount(100),
                new QuantityAmount(30),
                new QuantityAmount(70),
                null
        );

        assertThat(dailyQuantityAmount.quantityMemo()).isEqualTo(QuantityMemo.NONE);
    }

    @Test
    void 계획_달성량과_딜레이량의_합이_계획량과_다르면_예외를_던진다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(100, 30, 60))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_AMOUNT_MISMATCH);
    }

    @Test
    void 일일_계획량을_증가시킨다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70, "메모");

        DailyQuantityAmount increasedDailyQuantityAmount = dailyQuantityAmount.increaseBy(new QuantityAmount(20));

        assertThat(increasedDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(120));
        assertThat(increasedDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(30));
        assertThat(increasedDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(90));
        assertThat(increasedDailyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 일일_계획량을_감소시킨다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70, "메모");

        DailyQuantityAmount reducedDailyQuantityAmount = dailyQuantityAmount.reduceBy(new QuantityAmount(20));

        assertThat(reducedDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(80));
        assertThat(reducedDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(30));
        assertThat(reducedDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(50));
        assertThat(reducedDailyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 일일_계획량을_달성량보다_적게_감소시킬_수_없다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> dailyQuantityAmount.reduceBy(new QuantityAmount(80)))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
    }

    @Test
    void 일일_계획량을_딜레이량만큼_감소시킬_수_있다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        DailyQuantityAmount reducedDailyQuantityAmount = dailyQuantityAmount.reduceBy(new QuantityAmount(70));

        assertThat(reducedDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(30));
        assertThat(reducedDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(30));
        assertThat(reducedDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(QuantityAmount.ZERO);
    }

    @Test
    void 일일_계획_달성량을_늘린다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70, "메모");

        DailyQuantityAmount completedDailyQuantityAmount = dailyQuantityAmount.completeBy(new QuantityAmount(20));

        assertThat(completedDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(completedDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(50));
        assertThat(completedDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(50));
        assertThat(completedDailyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 일일_계획_달성량을_딜레이량만큼_늘릴_수_있다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        DailyQuantityAmount completedDailyQuantityAmount = dailyQuantityAmount.completeBy(new QuantityAmount(70));

        assertThat(completedDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(completedDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(completedDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(QuantityAmount.ZERO);
    }

    @Test
    void 일일_계획_달성량_증가분은_딜레이량보다_많을_수_없다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> dailyQuantityAmount.completeBy(new QuantityAmount(80)))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);
    }

    @Test
    void 일일_계획_달성량을_일부_취소한다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70, "메모");

        DailyQuantityAmount canceledDailyQuantityAmount =
                dailyQuantityAmount.cancelCompletionBy(new QuantityAmount(20));

        assertThat(canceledDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(canceledDailyQuantityAmount.completedQuantityAmount()).isEqualTo(new QuantityAmount(10));
        assertThat(canceledDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(90));
        assertThat(canceledDailyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 일일_계획_달성량을_모두_취소할_수_있다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        DailyQuantityAmount canceledDailyQuantityAmount =
                dailyQuantityAmount.cancelCompletionBy(new QuantityAmount(30));

        assertThat(canceledDailyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(canceledDailyQuantityAmount.completedQuantityAmount()).isEqualTo(QuantityAmount.ZERO);
        assertThat(canceledDailyQuantityAmount.delayedQuantityAmount()).isEqualTo(new QuantityAmount(100));
    }

    @Test
    void 일일_계획_달성량을_실제_달성량보다_많이_취소할_수_없다() {
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 30, 70);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> dailyQuantityAmount.cancelCompletionBy(new QuantityAmount(40)))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_AMOUNT_TO_CANCEL_COMPLETION_EXCEEDS_COMPLETED_AMOUNT);
    }
}
