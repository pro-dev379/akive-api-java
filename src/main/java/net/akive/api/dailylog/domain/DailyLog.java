package net.akive.api.dailylog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class DailyLog {

    private Long dailyLogId;

    private User user;

    private FocusTime focusTime;

    private DailyLogType dailyLogType;

    private String contents;

    public static DailyLog reconstruct(
            Long dailyLogId,
            User user,
            FocusTime focusTime,
            DailyLogType dailyLogType,
            String contents
    ) {
        return DailyLog.builder()
                .dailyLogId(requirePositive(dailyLogId))
                .user(requireNonNull(user))
                .focusTime(requireNonNull(focusTime))
                .dailyLogType(requireNonNull(dailyLogType))
                .contents(requireNotBlank(contents))
                .build();
    }

    public static DailyLog create(
            User user,
            FocusTime focusTime,
            DailyLogType dailyLogType,
            String contents
    ) {
        return DailyLog.builder()
                .user(requireNonNull(user))
                .focusTime(requireNonNull(focusTime))
                .dailyLogType(requireNonNull(dailyLogType))
                .contents(requireNotBlank(contents))
                .build();
    }

    public void update(
            FocusTime targetFocusTime,
            DailyLogType targetDailyLogType,
            String targetContents
    ) {
        this.focusTime = requireNonNull(targetFocusTime);
        this.dailyLogType = requireNonNull(targetDailyLogType);
        this.contents = requireNotBlank(targetContents);
    }

    public LocalDate getLogOn() {
        return focusTime.getLogOn();
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
}
