package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.FocusMetrics;

import java.math.BigDecimal;

public record FocusMetricsFindResponse(long focusTimeSec, BigDecimal focusRate) {

    public static FocusMetricsFindResponse from(FocusMetrics metrics) {
        return new FocusMetricsFindResponse(metrics.focusTimeSec(), metrics.focusRate());
    }
}
