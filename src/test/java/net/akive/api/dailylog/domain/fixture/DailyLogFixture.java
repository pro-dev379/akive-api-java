package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.ActivityTimeFixture.createActivityTime;

public class DailyLogFixture {

    private DailyLogFixture() {
    }

    public static final Long DAILY_LOG_ID = 1L;
    public static final Long ANOTHER_DAILY_LOG_ID = 2L;
    public static final Long UNKNOWN_DAILY_LOG_ID = Long.MAX_VALUE;
    public static final LocalDate LOG_ON = LocalDate.of(2026, 9, 12);
    public static final LocalDate ANOTHER_LOG_ON = LocalDate.of(2026, 9, 13);
    public static final LocalDateTime START_ON = LocalDateTime.of(2026, 9, 12, 9, 0);
    public static final LocalDateTime END_ON = LocalDateTime.of(2026, 9, 12, 10, 0);
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 23, 59);
    public static final Long FOCUS_TIME_SEC = 3_000L;
    public static final ActivityTime ACTIVITY_TIME = ActivityTime.of(START_ON, END_ON, FOCUS_TIME_SEC, false, NOW);
    public static final String CONTENTS = "오늘의 학습 기록";
    public static final LocalDate TARGET_LOG_ON = LocalDate.of(2026, 9, 14);
    public static final LocalDateTime TARGET_START_ON = LocalDateTime.of(2026, 9, 13, 11, 0);
    public static final LocalDateTime TARGET_END_ON = LocalDateTime.of(2026, 9, 13, 12, 0);
    public static final ActivityTime TARGET_ACTIVITY_TIME = ActivityTime.of(
            TARGET_START_ON,
            TARGET_END_ON,
            null,
            true,
            NOW
    );
    public static final String TARGET_CONTENTS = "수정된 학습 기록";

    public static DailyLog createReconstructedDailyLog(User user) {
        return DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                NOW
        );
    }

    public static DailyLog createDailyLog(User user, LocalDate logOn, int startHour) {
        return createDailyLog(user, createActivityTime(logOn, startHour));
    }

    public static DailyLog createDailyLog(User user, ActivityTime activityTime) {
        return DailyLog.create(
                user,
                activityTime,
                STUDY,
                CONTENTS,
                NOW
        );
    }

    public static LocalDateTime startOn(LocalDate logOn, int hour) {
        return logOn.atTime(hour, 0);
    }

    public static LocalDateTime endOn(LocalDate logOn, int hour) {
        return startOn(logOn, hour);
    }
}
