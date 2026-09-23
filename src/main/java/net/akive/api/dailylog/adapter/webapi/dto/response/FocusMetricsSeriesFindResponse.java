package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.FocusMetricsSeries;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FocusMetricsSeriesFindResponse(List<PeriodFocusMetricsResponse> periodFocusMetrics) {

    public static FocusMetricsSeriesFindResponse from(FocusMetricsSeries series) {
        return new FocusMetricsSeriesFindResponse(series.periodFocusMetrics().stream()
                .map(PeriodFocusMetricsResponse::from)
                .toList());
    }

    public record PeriodFocusMetricsResponse(
            LocalDate startOn,
            LocalDate endOn,
            long focusTimeSec,
            BigDecimal focusRate
    ) {

        private static PeriodFocusMetricsResponse from(FocusMetricsSeries.PeriodFocusMetrics periodFocusMetrics) {
            return new PeriodFocusMetricsResponse(
                    periodFocusMetrics.startOn(),
                    periodFocusMetrics.endOn(),
                    periodFocusMetrics.focusMetrics().focusTimeSec(),
                    periodFocusMetrics.focusMetrics().focusRate()
            );
        }
    }
}
