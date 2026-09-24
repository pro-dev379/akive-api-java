package net.akive.api.plan.domain.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.analytics.TimeSeriesUnit.*;
import static net.akive.api.plan.domain.fixture.CompletionRateFixture.START_ON;
import static net.akive.api.plan.domain.fixture.CompletionRateFixture.createDailyCompletionAmount;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.SECOND_TOTAL_QUANTITY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class CompletionRateSeriesTest {

    @Test
    void 일_단위로_각_날짜의_달성률을_계산한다() {
        LocalDate nextOn = START_ON.plusDays(1);
        List<TotalQuantityDailyCompletionAmount> dailyCompletionAmounts = List.of(
                createDailyCompletionAmount(FIRST_TOTAL_QUANTITY_ID, START_ON, 100, 100),
                createDailyCompletionAmount(SECOND_TOTAL_QUANTITY_ID, START_ON, 100, 0),
                createDailyCompletionAmount(FIRST_TOTAL_QUANTITY_ID, nextOn, 100, 100),
                createDailyCompletionAmount(SECOND_TOTAL_QUANTITY_ID, nextOn, 100, 50)
        );

        CompletionRateSeries completionRateSeries = CompletionRateSeries.calculate(
                START_ON,
                nextOn,
                DAY,
                dailyCompletionAmounts
        );

        assertThat(completionRateSeries.periodCompletionRates())
                .extracting(
                        CompletionRateSeries.PeriodCompletionRate::startOn,
                        periodCompletionRate -> periodCompletionRate.completionRate().value()
                )
                .containsExactly(
                        tuple(START_ON, new BigDecimal("50.0")),
                        tuple(nextOn, new BigDecimal("75.0"))
                );
    }

    @Test
    void 달성률은_일별_달성률의_평균이_아니라_세션_총량별_계획량과_완료량을_합산해_계산한다() {
        List<TotalQuantityDailyCompletionAmount> completionAmounts = List.of(
                createDailyCompletionAmount(FIRST_TOTAL_QUANTITY_ID, START_ON, 100, 100),
                createDailyCompletionAmount(FIRST_TOTAL_QUANTITY_ID, START_ON.plusDays(1), 900, 0)
        );

        CompletionRateSeries series = CompletionRateSeries.calculate(
                START_ON,
                START_ON.plusDays(6),
                WEEK,
                completionAmounts
        );

        assertThat(series.periodCompletionRates().getFirst().completionRate().value())
                .as("날짜별 달성률의 평균이 아니라 세션 총량의 합산 계획량 1,000 중 100을 달성한 것으로 계산한다")
                .isEqualByComparingTo("10.0");
    }

    @Test
    void 주_단위는_일요일을_종료일로_하고_첫째와_마지막_주에는_조회_기간에_포함된_날짜만_집계한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 16);
        LocalDate endOn = LocalDate.of(2026, 9, 22);

        CompletionRateSeries series = CompletionRateSeries.calculate(startOn, endOn, WEEK, List.of());

        assertThat(series.periodCompletionRates())
                .extracting(
                        CompletionRateSeries.PeriodCompletionRate::startOn,
                        CompletionRateSeries.PeriodCompletionRate::endOn
                )
                .containsExactly(
                        tuple(startOn, LocalDate.of(2026, 9, 20)),
                        tuple(LocalDate.of(2026, 9, 21), endOn)
                );
    }

    @Test
    void 월_단위는_월말을_종료일로_구분하고_첫째와_마지막_달에는_조회_기간에_포함된_날짜만_집계한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 14);
        LocalDate endOn = LocalDate.of(2026, 10, 3);

        CompletionRateSeries series = CompletionRateSeries.calculate(startOn, endOn, MONTH, List.of());

        assertThat(series.periodCompletionRates())
                .extracting(
                        CompletionRateSeries.PeriodCompletionRate::startOn,
                        CompletionRateSeries.PeriodCompletionRate::endOn
                )
                .containsExactly(
                        tuple(startOn, LocalDate.of(2026, 9, 30)),
                        tuple(LocalDate.of(2026, 10, 1), endOn)
                );
    }
}
