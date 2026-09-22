package net.akive.api.dailylog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.dailylog.domain.DailyLogPolicy.DAY_START_HOUR;
import static net.akive.api.dailylog.domain.DailyLogPolicy.MIN_SLEEP_DURATION_SEC;
import static net.akive.api.dailylog.domain.DailyLogType.SLEEP;
import static net.akive.api.dailylog.domain.DailyLogType.STUDY;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class DailyLog {

    private Long dailyLogId;

    private User user;

    private LocalDate logOn;

    private ActivityTime activityTime;

    private DailyLogType dailyLogType;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            LocalDate logOn,
            ActivityTime activityTime,
            DailyLogType dailyLogType,
            String contents,
            LocalDateTime reconstructedAt
    ) {
        requireNonNull(activityTime);
        requireNonNull(dailyLogType);
        validateLogOnNotFuture(logOn, reconstructedAt);
        validateSleepTime(activityTime, dailyLogType);
        validateFocusTime(activityTime, dailyLogType);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .activityTime(normalizeActivityTime(activityTime, dailyLogType))
                .dailyLogType(dailyLogType)
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            ActivityTime activityTime,
            DailyLogType dailyLogType,
            String contents,
            LocalDateTime requestedAt
    ) {
        requireNonNull(activityTime);
        requireNonNull(dailyLogType);
        LocalDate logOn = resolveLogOn(activityTime, dailyLogType);
        validateLogOnNotFuture(logOn, requestedAt);
        validateSleepTime(activityTime, dailyLogType);
        validateFocusTime(activityTime, dailyLogType);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(logOn)
                .activityTime(normalizeActivityTime(activityTime, dailyLogType))
                .dailyLogType(dailyLogType)
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            ActivityTime targetActivityTime,
            DailyLogType targetDailyLogType,
            String targetContents,
            LocalDateTime requestedAt
    ) {
        requireNonNull(targetActivityTime);
        requireNonNull(targetDailyLogType);
        LocalDate targetLogOn = resolveLogOn(targetActivityTime, targetDailyLogType);
        validateLogOnNotFuture(targetLogOn, requestedAt);
        validateSleepTime(targetActivityTime, targetDailyLogType);
        validateFocusTime(targetActivityTime, targetDailyLogType);

        this.logOn = targetLogOn;
        this.activityTime = normalizeActivityTime(targetActivityTime, targetDailyLogType);
        this.dailyLogType = targetDailyLogType;
        this.contents = requireNotBlank(targetContents);
    }

    public LocalDateTime getStartOn() {
        return activityTime.getStartOn();
    }

    public LocalDateTime getEndOn() {
        return activityTime.getEndOn();
    }

    public Long getFocusTimeSec() {
        return activityTime.getFocusTimeSec();
    }

    public boolean isFocusRateExcluded() {
        return activityTime.isFocusRateExcluded();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DailyLog that = (DailyLog) o;

        return this.dailyLogId != null && Objects.equals(this.dailyLogId, that.dailyLogId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static void validateLogOnNotFuture(LocalDate logOn, LocalDateTime baseTime) {
        requireNonNull(logOn);
        requireNonNull(baseTime);

        if (logOn.isAfter(baseTime.toLocalDate())) {
            throw new BusinessRuleViolationException(DAILY_LOG_DATE_IN_FUTURE);
        }
    }

    private static LocalDate resolveLogOn(ActivityTime activityTime, DailyLogType dailyLogType) {
        if (dailyLogType == SLEEP) {
            return activityTime.getEndOn().toLocalDate();
        }

        return activityTime.getStartOn().minusHours(DAY_START_HOUR).toLocalDate();
    }

    private static void validateSleepTime(ActivityTime activityTime, DailyLogType dailyLogType) {
        if (dailyLogType == SLEEP && activityTime.durationSec() < MIN_SLEEP_DURATION_SEC) {
            throw new BusinessRuleViolationException(DAILY_LOG_SLEEP_TIME_TOO_SHORT);
        }
    }

    private static void validateFocusTime(ActivityTime activityTime, DailyLogType dailyLogType) {
        if (dailyLogType != STUDY && activityTime.getFocusTimeSec() != null) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
        }
    }

    private static ActivityTime normalizeActivityTime(ActivityTime activityTime, DailyLogType dailyLogType) {
        return dailyLogType == SLEEP ? activityTime.excludingFromFocusRate() : activityTime;
    }
}
