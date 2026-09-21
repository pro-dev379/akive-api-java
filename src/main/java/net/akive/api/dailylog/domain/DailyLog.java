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

    private FocusTime focusTime;

    private DailyLogType dailyLogType;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            LocalDate logOn,
            FocusTime focusTime,
            DailyLogType dailyLogType,
            String contents,
            LocalDateTime reconstructedAt
    ) {
        requireNonNull(focusTime);
        requireNonNull(dailyLogType);
        validateLogOnNotFuture(logOn, reconstructedAt);
        validateSleepTime(focusTime, dailyLogType);
        validateFocusTime(focusTime, dailyLogType);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .focusTime(normalizeFocusTime(focusTime, dailyLogType))
                .dailyLogType(dailyLogType)
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            FocusTime focusTime,
            DailyLogType dailyLogType,
            String contents,
            LocalDateTime requestedAt
    ) {
        requireNonNull(focusTime);
        requireNonNull(dailyLogType);
        LocalDate logOn = resolveLogOn(focusTime, dailyLogType);
        validateLogOnNotFuture(logOn, requestedAt);
        validateSleepTime(focusTime, dailyLogType);
        validateFocusTime(focusTime, dailyLogType);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(logOn)
                .focusTime(normalizeFocusTime(focusTime, dailyLogType))
                .dailyLogType(dailyLogType)
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            FocusTime targetFocusTime,
            DailyLogType targetDailyLogType,
            String targetContents,
            LocalDateTime requestedAt
    ) {
        requireNonNull(targetFocusTime);
        requireNonNull(targetDailyLogType);
        LocalDate targetLogOn = resolveLogOn(targetFocusTime, targetDailyLogType);
        validateLogOnNotFuture(targetLogOn, requestedAt);
        validateSleepTime(targetFocusTime, targetDailyLogType);
        validateFocusTime(targetFocusTime, targetDailyLogType);

        this.logOn = targetLogOn;
        this.focusTime = normalizeFocusTime(targetFocusTime, targetDailyLogType);
        this.dailyLogType = targetDailyLogType;
        this.contents = requireNotBlank(targetContents);
    }

    public LocalDateTime getStartOn() {
        return focusTime.getStartOn();
    }

    public LocalDateTime getEndOn() {
        return focusTime.getEndOn();
    }

    public Long getFocusTimeSec() {
        return focusTime.getFocusTimeSec();
    }

    public boolean isFocusRateExcluded() {
        return focusTime.isFocusRateExcluded();
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

    private static LocalDate resolveLogOn(FocusTime focusTime, DailyLogType dailyLogType) {
        if (dailyLogType == SLEEP) {
            return focusTime.getEndOn().toLocalDate();
        }

        return focusTime.getStartOn().minusHours(DAY_START_HOUR).toLocalDate();
    }

    private static void validateSleepTime(FocusTime focusTime, DailyLogType dailyLogType) {
        if (dailyLogType == SLEEP && focusTime.durationSec() < MIN_SLEEP_DURATION_SEC) {
            throw new BusinessRuleViolationException(DAILY_LOG_SLEEP_TIME_TOO_SHORT);
        }
    }

    private static void validateFocusTime(FocusTime focusTime, DailyLogType dailyLogType) {
        if (dailyLogType != STUDY && focusTime.getFocusTimeSec() != null) {
            throw new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
        }
    }

    private static FocusTime normalizeFocusTime(FocusTime focusTime, DailyLogType dailyLogType) {
        return dailyLogType == SLEEP ? focusTime.excludingFromFocusRate() : focusTime;
    }
}
