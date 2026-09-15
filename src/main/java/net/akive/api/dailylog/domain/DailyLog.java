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
import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_DATE_IN_FUTURE;

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
        validateLogOnNotFuture(logOn, reconstructedAt);

        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .focusTime(requireNonNull(focusTime))
                .dailyLogType(requireNonNull(dailyLogType))
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            LocalDate logOn,
            FocusTime focusTime,
            DailyLogType dailyLogType,
            String contents,
            LocalDateTime requestedAt
    ) {
        validateLogOnNotFuture(logOn, requestedAt);

        return DailyLog.builder()
                .user(requireNonNull(user))
                .logOn(requireNonNull(logOn))
                .focusTime(requireNonNull(focusTime))
                .dailyLogType(requireNonNull(dailyLogType))
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            LocalDate targetLogOn,
            FocusTime targetFocusTime,
            DailyLogType targetDailyLogType,
            String targetContents,
            LocalDateTime requestedAt
    ) {
        validateLogOnNotFuture(targetLogOn, requestedAt);

        this.logOn = requireNonNull(targetLogOn);
        this.focusTime = requireNonNull(targetFocusTime);
        this.dailyLogType = requireNonNull(targetDailyLogType);
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
}
