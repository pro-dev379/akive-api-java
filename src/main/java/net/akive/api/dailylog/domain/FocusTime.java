package net.akive.api.dailylog.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;

@Getter
@EqualsAndHashCode
public final class FocusTime {

    private static final int MAX_TIME_RANGE_HOURS = 24;

    private final LocalDateTime startOn;

    private final LocalDateTime endOn;

    private final Long focusTimeSec;

    private final boolean focusRateExcluded;

    private FocusTime(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            LocalDateTime baseTime
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateMaxPeriod(startOn, endOn);
        validateNotFuture(endOn, baseTime);
        validateFocusTimeSec(startOn, endOn, focusTimeSec);

        this.startOn = startOn;
        this.endOn = endOn;
        this.focusTimeSec = requireNonNegativeIfPresent(focusTimeSec);
        this.focusRateExcluded = focusRateExcluded;
    }

    public static FocusTime of(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            LocalDateTime baseTime
    ) {
        return new FocusTime(startOn, endOn, focusTimeSec, focusRateExcluded, baseTime);
    }

    public long durationSec() {
        return Duration.between(startOn, endOn).toSeconds();
    }

    private static void validateMaxPeriod(LocalDateTime startOn, LocalDateTime endOn) {
        Duration timeRange = Duration.between(startOn, endOn);
        Duration maxTimeRange = Duration.ofHours(MAX_TIME_RANGE_HOURS);

        if (timeRange.compareTo(maxTimeRange) > 0) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_TOO_LONG);
        }
    }

    private static void validateNotFuture(LocalDateTime endOn, LocalDateTime baseTime) {
        requireNonNull(baseTime);

        if (endOn.isAfter(baseTime)) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE);
        }
    }

    private static void validateFocusTimeSec(LocalDateTime startOn, LocalDateTime endOn, Long focusTimeSec) {
        if (focusTimeSec != null && Duration.between(startOn, endOn).toSeconds() < focusTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
        }
    }
}
