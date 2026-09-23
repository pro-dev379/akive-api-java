package net.akive.api.dailylog.application.provided.dto;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record FocusMetricsFindQuery(Long userId, LocalDate startOn, LocalDate endOn) {

    public FocusMetricsFindQuery {
        requirePositive(userId);
        requireStartNotAfterEnd(startOn, endOn);
    }
}
