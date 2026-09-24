package net.akive.api.plan.adapter.webapi.sessionalplan.dto.response;

import net.akive.api.plan.domain.value.CompletionRateSeries;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CompletionRateSeriesFindResponse(List<PeriodCompletionRateResponse> periodCompletionRates) {

    public static CompletionRateSeriesFindResponse from(CompletionRateSeries series) {
        return new CompletionRateSeriesFindResponse(series.periodCompletionRates().stream()
                .map(PeriodCompletionRateResponse::from)
                .toList());
    }

    public record PeriodCompletionRateResponse(
            LocalDate startOn,
            LocalDate endOn,
            BigDecimal completionRate
    ) {

        private static PeriodCompletionRateResponse from(
                CompletionRateSeries.PeriodCompletionRate periodCompletionRate
        ) {
            return new PeriodCompletionRateResponse(
                    periodCompletionRate.startOn(),
                    periodCompletionRate.endOn(),
                    periodCompletionRate.completionRate().value()
            );
        }
    }
}
