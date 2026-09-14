package net.akive.api.dailylog.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;

@Getter
@EqualsAndHashCode
public final class FocusTime {

    private static final int MAX_TIME_RANGE_HOURS = 24;

    private final LocalDate logOn;

    private final LocalDateTime startOn;

    private final LocalDateTime endOn;

    private final Long focusTimeSec;

    private final boolean focusRateExcluded;

    private FocusTime(
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            LocalDateTime now
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateMaxPeriod(startOn, endOn);
        validateNotFuture(logOn, endOn, now);
        validateFocusTimeSec(startOn, endOn, focusTimeSec);

        this.logOn = requireNonNull(logOn);
        this.startOn = startOn;
        this.endOn = endOn;
        this.focusTimeSec = requireNonNegativeIfPresent(focusTimeSec);
        this.focusRateExcluded = focusRateExcluded;
    }

    public static FocusTime of(
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            LocalDateTime now
    ) {
        return new FocusTime(logOn, startOn, endOn, focusTimeSec, focusRateExcluded, now);
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

    private static void validateNotFuture(LocalDate logOn, LocalDateTime endOn, LocalDateTime now) {
        requireNonNull(logOn);
        requireNonNull(now);

        if (logOn.isAfter(now.toLocalDate())) {
            throw new BusinessRuleViolationException(DAILY_LOG_DATE_IN_FUTURE);
        }

        if (endOn.isAfter(now)) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE);
        }
    }

    private static void validateFocusTimeSec(LocalDateTime startOn, LocalDateTime endOn, Long focusTimeSec) {
        if (focusTimeSec != null && Duration.between(startOn, endOn).toSeconds() < focusTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
        }
    }
}
