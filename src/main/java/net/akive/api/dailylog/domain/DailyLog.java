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
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_INTENSIVE_TIME_EXCEEDS_DURATION;

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

    private Long intensiveTimeSec;

    private boolean intensiveExcluded;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            DailyLogType dailyLogType,
            Long intensiveTimeSec,
            boolean intensiveExcluded,
            String contents
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateIntensiveTimeSec(startOn, endOn, intensiveTimeSec);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .startOn(requireNonNull(startOn))
                .endOn(requireNonNull(endOn))
                .dailyLogType(requireNonNull(dailyLogType))
                .intensiveTimeSec(requireNonNegativeIfPresent(intensiveTimeSec))
                .intensiveExcluded(intensiveExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            LocalDate logOn,
            LocalDateTime startOn,
            LocalDateTime endOn,
            DailyLogType dailyLogType,
            Long intensiveTimeSec,
            boolean intensiveExcluded,
            String contents
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        validateIntensiveTimeSec(startOn, endOn, intensiveTimeSec);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .startOn(requireNonNull(startOn))
                .endOn(requireNonNull(endOn))
                .dailyLogType(requireNonNull(dailyLogType))
                .intensiveTimeSec(requireNonNegativeIfPresent(intensiveTimeSec))
                .intensiveExcluded(intensiveExcluded)
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            LocalDate targetLogOn,
            LocalDateTime targetStartOn,
            LocalDateTime targetEndOn,
            DailyLogType targetDailyLogType,
            Long targetIntensiveTimeSec,
            boolean targetIntensiveExcluded,
            String targetContents
    ) {
        requireStartNotAfterEnd(targetStartOn, targetEndOn);
        validateIntensiveTimeSec(targetStartOn, targetEndOn, targetIntensiveTimeSec);

        this.logOn = requireNonNull(targetLogOn);
        this.startOn = requireNonNull(targetStartOn);
        this.endOn = requireNonNull(targetEndOn);
        this.dailyLogType = requireNonNull(targetDailyLogType);
        this.intensiveTimeSec = requireNonNegativeIfPresent(targetIntensiveTimeSec);
        this.intensiveExcluded = targetIntensiveExcluded;
        this.contents = requireNotBlank(targetContents);
    }

    public static long calculateDailyLogDurationSec(LocalDateTime startOn, LocalDateTime endOn) {
        requireNonNull(startOn);
        requireNonNull(endOn);

        return Duration.between(startOn, endOn).toSeconds();
    }

    private static void validateIntensiveTimeSec(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long intensiveTimeSec
    ) {
        if (intensiveTimeSec != null && calculateDailyLogDurationSec(startOn, endOn) < intensiveTimeSec) {
            throw new BusinessRuleViolationException(DAILY_LOG_INTENSIVE_TIME_EXCEEDS_DURATION);
        }
    }
}
