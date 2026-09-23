package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.FocusMetricsSeries;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FocusMetricsSeriesFindResponse(List<PeriodMetricsResponse> focusMetrics) {

    public static FocusMetricsSeriesFindResponse from(FocusMetricsSeries series) {
        return new FocusMetricsSeriesFindResponse(series.periodFocusMetrics().stream()
                .map(PeriodMetricsResponse::from)
                .toList());
    }

    public record PeriodMetricsResponse(
            LocalDate startOn,
            LocalDate endOn,
            long focusTimeSec,
            BigDecimal focusRate
    ) {

        private static PeriodMetricsResponse from(FocusMetricsSeries.PeriodFocusMetrics periodFocusMetrics) {
            return new PeriodMetricsResponse(
                    periodFocusMetrics.startOn(),
                    periodFocusMetrics.endOn(),
                    periodFocusMetrics.focusMetrics().focusTimeSec(),
                    periodFocusMetrics.focusMetrics().focusRate()
            );
        }
    }
}
