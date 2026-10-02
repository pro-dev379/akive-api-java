package net.akive.api.dailylog.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;

@Getter
@EqualsAndHashCode
public final class ActivityTime {

    private static final int MAX_TIME_RANGE_HOURS = 24;

    private final LocalDateTime startOn;

    private final LocalDateTime endOn;

    private final Long focusTimeSec;

    private final boolean focusRateExcluded;

    private ActivityTime(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            Instant baseTime
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

    public static ActivityTime of(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded,
            Instant baseTime
    ) {
        return new ActivityTime(startOn, endOn, focusTimeSec, focusRateExcluded, baseTime);
    }

    public long durationSec() {
        return Duration.between(this.startOn, this.endOn).toSeconds();
    }

    public ActivityTime excludingFromFocusRate() {
        if (this.focusRateExcluded) {
            return this;
        }

        return new ActivityTime(
                this.startOn,
                this.endOn,
                this.focusTimeSec,
                true,
                toInstantAtServiceZone(this.endOn)
        );
    }

    private static void validateNotFuture(LocalDateTime endOn, Instant baseTime) {
        requireNonNull(baseTime);

        if (toInstantAtServiceZone(endOn).isAfter(baseTime)) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE);
        }
    }

    private static void validateMaxPeriod(LocalDateTime startOn, LocalDateTime endOn) {
        Duration timeRange = Duration.between(startOn, endOn);
        Duration maxTimeRange = Duration.ofHours(MAX_TIME_RANGE_HOURS);

        if (timeRange.compareTo(maxTimeRange) > 0) {
            throw new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_TOO_LONG);
        }
    }

    private static void validateFocusTimeSec(LocalDateTime startOn, LocalDateTime endOn, Long focusTimeSec) {
        if (focusTimeSec != null && Duration.between(startOn, endOn).toSeconds() < focusTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
        }
    }
}
