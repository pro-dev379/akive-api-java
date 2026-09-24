package net.akive.api.plan.application.provided.sessionalplan.dto;

import net.akive.api.common.analytics.TimeSeriesUnit;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record CompletionRateSeriesFindQuery(
        Long userId,
        LocalDate startOn,
        LocalDate endOn,
        TimeSeriesUnit unit
) {

    public CompletionRateSeriesFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
        requireNonNull(unit);
    }
}
