package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.common.analytics.TimeSeriesUnit;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record FocusMetricsSeriesFindQuery(
        Long userId,
        LocalDate startOn,
        LocalDate endOn,
        TimeSeriesUnit unit
) {

    public FocusMetricsSeriesFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
        requireNonNull(unit);
    }
}
