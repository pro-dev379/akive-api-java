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

    private LocalDateTime startOn;

    private LocalDateTime endOn;

    private DailyLogType dailyLogType;

    private Long focusTimeSec;

    private boolean focusRateExcluded;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            DailyLogType dailyLogType,
            Long focusTimeSec,
            boolean focusRateExcluded,
            String contents
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateFocusTimeSec(startOn, endOn, focusTimeSec);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .startOn(requireNonNull(startOn))
                .endOn(requireNonNull(endOn))
                .dailyLogType(requireNonNull(dailyLogType))
                .focusTimeSec(requireNonNegativeIfPresent(focusTimeSec))
                .focusRateExcluded(focusRateExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            DailyLogType dailyLogType,
            Long focusTimeSec,
            boolean focusRateExcluded,
            String contents
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateFocusTimeSec(startOn, endOn, focusTimeSec);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .startOn(requireNonNull(startOn))
                .endOn(requireNonNull(endOn))
                .dailyLogType(requireNonNull(dailyLogType))
                .focusTimeSec(requireNonNegativeIfPresent(focusTimeSec))
                .focusRateExcluded(focusRateExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            LocalDate targetLogOn,
            LocalDateTime targetStartOn,
            LocalDateTime targetEndOn,
            DailyLogType targetDailyLogType,
            Long targetFocusTimeSec,
            boolean targetFocusRateExcluded,
            String targetContents
    ) {
        requireStartNotAfterEnd(targetStartOn, targetEndOn);
        validateFocusTimeSec(targetStartOn, targetEndOn, targetFocusTimeSec);

        this.logOn = requireNonNull(targetLogOn);
        this.startOn = requireNonNull(targetStartOn);
        this.endOn = requireNonNull(targetEndOn);
        this.dailyLogType = requireNonNull(targetDailyLogType);
        this.focusTimeSec = requireNonNegativeIfPresent(targetFocusTimeSec);
        this.focusRateExcluded = targetFocusRateExcluded;
        this.contents = requireNotBlank(targetContents);
    }

    public static long calculateDailyLogDurationSec(LocalDateTime startOn, LocalDateTime endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        return Duration.between(startOn, endOn).toSeconds();
    }

    private static void validateFocusTimeSec(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec
    ) {
        if (focusTimeSec != null && calculateDailyLogDurationSec(startOn, endOn) < focusTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
        }
    }
}
