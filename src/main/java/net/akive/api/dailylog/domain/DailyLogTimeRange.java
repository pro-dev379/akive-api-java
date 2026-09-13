package net.akive.api.dailylog.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_TIME_RANGE_IN_FUTURE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_TIME_RANGE_TOO_LONG;

@Getter
@EqualsAndHashCode
public final class DailyLogTimeRange {

    private static final int MAX_TIME_RANGE_HOURS = 24;

    private final LocalDateTime startOn;
    private final LocalDateTime endOn;

    private DailyLogTimeRange(LocalDateTime startOn, LocalDateTime endOn, LocalDateTime now) {
        requireStartNotAfterEnd(startOn, endOn);
        validateMaxPeriod(startOn, endOn);
        validateNotFuture(endOn, now);

        this.startOn = startOn;
        this.endOn = endOn;
    }

    public static DailyLogTimeRange of(LocalDateTime startOn, LocalDateTime endOn, LocalDateTime now) {
        return new DailyLogTimeRange(startOn, endOn, now);
    }

    private static void validateMaxPeriod(LocalDateTime startOn, LocalDateTime endOn) {
        Duration timeRange = Duration.between(startOn, endOn);
        Duration maxTimeRange = Duration.ofHours(MAX_TIME_RANGE_HOURS);

        if (timeRange.compareTo(maxTimeRange) > 0) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_TOO_LONG);
        }
    }

    private static void validateNotFuture(LocalDateTime endOn, LocalDateTime now) {
        requireNonNull(now);

        if (endOn.isAfter(now)) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE);
        }
    }
}
