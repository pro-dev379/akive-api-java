package net.akive.api.dailylog.application.provided.dto;

import net.akive.api.dailylog.domain.FocusMetricsUnit;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record FocusMetricsSeriesFindQuery(
        Long userId,
        LocalDate startOn,
        LocalDate endOn,
        FocusMetricsUnit unit
) {

    public FocusMetricsSeriesFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
        requireNonNull(unit);
    }
}
