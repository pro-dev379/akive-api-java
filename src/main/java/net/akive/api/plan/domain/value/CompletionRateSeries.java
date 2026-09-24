package net.akive.api.plan.domain.value;

import net.akive.api.common.analytics.TimeSeriesUnit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record CompletionRateSeries(List<PeriodCompletionRate> periodCompletionRates) {

    public CompletionRateSeries {
        periodCompletionRates = List.copyOf(periodCompletionRates);
    }

    public static CompletionRateSeries calculate(
            LocalDate startOn,
            LocalDate endOn,
            TimeSeriesUnit unit,
            List<TotalQuantityDailyCompletionAmount> dailyCompletionAmounts
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        requireNonNull(unit);
        requireNonNull(dailyCompletionAmounts);

        List<PeriodCompletionRate> periodCompletionRates = new ArrayList<>();
        LocalDate periodStartOn = startOn;

        while (!periodStartOn.isAfter(endOn)) {
            LocalDate periodEndOn = min(unit.periodEndOn(periodStartOn), endOn);
            CompletionRate completionRate = calculatePeriod(
                    periodStartOn,
                    periodEndOn,
                    dailyCompletionAmounts
            );

            periodCompletionRates.add(new PeriodCompletionRate(periodStartOn, periodEndOn, completionRate));
            periodStartOn = periodEndOn.plusDays(1);
        }

        return new CompletionRateSeries(periodCompletionRates);
    }

    private static CompletionRate calculatePeriod(
            LocalDate periodStartOn,
            LocalDate periodEndOn,
            List<TotalQuantityDailyCompletionAmount> dailyCompletionAmounts
    ) {
        Map<Long, List<TotalQuantityDailyCompletionAmount>> completionAmountsByTotalQuantityId =
                dailyCompletionAmounts.stream()
                        .filter(completionAmount ->
                                !completionAmount.date().isBefore(periodStartOn)
                                        && !completionAmount.date().isAfter(periodEndOn)
                        )
                        .collect(Collectors.groupingBy(TotalQuantityDailyCompletionAmount::totalQuantityId));

        List<QuantityCompletionAmount> completionAmounts = completionAmountsByTotalQuantityId.values().stream()
                .map(CompletionRateSeries::sumCompletionAmounts)
                .toList();

        return CompletionRate.calculate(completionAmounts);
    }

    private static QuantityCompletionAmount sumCompletionAmounts(
            List<TotalQuantityDailyCompletionAmount> dailyCompletionAmounts
    ) {
        long quantityAmount = dailyCompletionAmounts.stream()
                .map(TotalQuantityDailyCompletionAmount::completionAmount)
                .mapToLong(QuantityCompletionAmount::quantityAmount)
                .sum();
        long completedQuantityAmount = dailyCompletionAmounts.stream()
                .map(TotalQuantityDailyCompletionAmount::completionAmount)
                .mapToLong(QuantityCompletionAmount::completedQuantityAmount)
                .sum();

        return new QuantityCompletionAmount(
                Math.toIntExact(quantityAmount),
                Math.toIntExact(completedQuantityAmount)
        );
    }

    private static LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    public record PeriodCompletionRate(LocalDate startOn, LocalDate endOn, CompletionRate completionRate) {
    }
}
