package net.akive.api.dailylog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class DailyLog {

    private Long dailyLogId;

    private User user;

    private LocalDate logOn;

    private DailyLogTimeRange timeRange;

    private DailyLogType dailyLogType;

    private Long focusTimeSec;

    private boolean focusRateExcluded;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            LocalDate logOn,
            DailyLogTimeRange timeRange,
            DailyLogType dailyLogType,
            Long focusTimeSec,
            boolean focusRateExcluded,
            String contents
    ) {
        validateFocusTimeSec(timeRange, focusTimeSec);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .timeRange(requireNonNull(timeRange))
                .dailyLogType(requireNonNull(dailyLogType))
                .focusTimeSec(requireNonNegativeIfPresent(focusTimeSec))
                .focusRateExcluded(focusRateExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            LocalDate logOn,
            DailyLogTimeRange timeRange,
            DailyLogType dailyLogType,
            Long focusTimeSec,
            boolean focusRateExcluded,
            String contents
    ) {
        validateFocusTimeSec(timeRange, focusTimeSec);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .timeRange(requireNonNull(timeRange))
                .dailyLogType(requireNonNull(dailyLogType))
                .focusTimeSec(requireNonNegativeIfPresent(focusTimeSec))
                .focusRateExcluded(focusRateExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            LocalDate targetLogOn,
            DailyLogTimeRange targetTimeRange,
            DailyLogType targetDailyLogType,
            Long targetFocusTimeSec,
            boolean targetFocusRateExcluded,
            String targetContents
    ) {
        validateFocusTimeSec(targetTimeRange, targetFocusTimeSec);

        this.logOn = requireNonNull(targetLogOn);
        this.timeRange = requireNonNull(targetTimeRange);
        this.dailyLogType = requireNonNull(targetDailyLogType);
        this.focusTimeSec = requireNonNegativeIfPresent(targetFocusTimeSec);
        this.focusRateExcluded = targetFocusRateExcluded;
        this.contents = requireNotBlank(targetContents);
    }

    public LocalDateTime getStartOn() {
        return timeRange.getStartOn();
    }

    public LocalDateTime getEndOn() {
        return timeRange.getEndOn();
    }

    public static long calculateDailyLogDurationSec(DailyLogTimeRange timeRange) {
        requireNonNull(timeRange);

        return Duration.between(timeRange.getStartOn(), timeRange.getEndOn()).toSeconds();
    }

    private static void validateFocusTimeSec(DailyLogTimeRange timeRange, Long focusTimeSec) {
        if (focusTimeSec != null && calculateDailyLogDurationSec(timeRange) < focusTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
        }
    }
}
