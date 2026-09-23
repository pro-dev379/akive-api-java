package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.dailylog.domain.DailyLogType.*;
import static net.akive.api.dailylog.domain.fixture.ActivityTimeFixture.createActivityTime;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

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
    public static final String TARGET_CONTENTS = "수정된 학습 기록";
    private static final long MINUTE_SECONDS = 60L;
    private static final long HOUR_SECONDS = 60 * MINUTE_SECONDS;

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
        return createDailyLog(user, activityTime, STUDY, CONTENTS);
    }

    public static DailyLog createDailyLog(
            LocalDate date,
            int startHour,
            int endHour,
            Long focusTimeSec,
            DailyLogType dailyLogType,
            boolean focusRateExcluded
    ) {
        return createDailyLog(
                date.atTime(startHour, 0),
                date.atTime(endHour, 0),
                focusTimeSec,
                dailyLogType,
                focusRateExcluded
        );
    }

    public static DailyLog createDailyLog(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            DailyLogType dailyLogType,
            boolean focusRateExcluded
    ) {
        ActivityTime activityTime = ActivityTime.of(
                startOn,
                endOn,
                focusTimeSec,
                focusRateExcluded,
                NOW
        );

        return createDailyLog(createUser(), activityTime, dailyLogType, dailyLogType.getLabel());
    }

    public static DailyLog createDailyLog(
            User user,
            ActivityTime activityTime,
            DailyLogType dailyLogType,
            String contents
    ) {
        return DailyLog.create(
                user,
                activityTime,
                dailyLogType,
                contents,
                NOW
        );
    }

    public static LocalDateTime startOn(LocalDate logOn, int hour) {
        return logOn.atTime(hour, 0);
    }

    public static LocalDateTime endOn(LocalDate logOn, int hour) {
        return startOn(logOn, hour);
    }

    public static long minutes(long minutes) {
        return minutes * MINUTE_SECONDS;
    }

    public static long hours(long hours) {
        return hours * HOUR_SECONDS;
    }

    public static DailyLog study(LocalDate date, int startHour, int endHour, long focusTimeSec) {
        return createDailyLog(date, startHour, endHour, focusTimeSec, STUDY, false);
    }

    public static DailyLog study(LocalDateTime startOn, LocalDateTime endOn, Long focusTimeSec) {
        return createDailyLog(startOn, endOn, focusTimeSec, STUDY, false);
    }

    public static DailyLog meal(LocalDate date, int startHour, int endHour) {
        return createDailyLog(date, startHour, endHour, null, MEAL, false);
    }

    public static DailyLog rest(LocalDate date, int startHour, int endHour) {
        return createDailyLog(date, startHour, endHour, null, REST, false);
    }

    public static DailyLog sleep(LocalDate date, int startHour, int endHour) {
        return createDailyLog(date, startHour, endHour, null, SLEEP, false);
    }

    public static DailyLog sleep(LocalDateTime startOn, LocalDateTime endOn) {
        return createDailyLog(startOn, endOn, null, SLEEP, false);
    }

    public static DailyLog etc(LocalDate date, int startHour, int endHour, boolean excludedFromFocusRate) {
        return createDailyLog(date, startHour, endHour, null, ETC, excludedFromFocusRate);
    }

    public static DailyLog etc(
            LocalDateTime startOn,
            LocalDateTime endOn,
            boolean excludedFromFocusRate
    ) {
        return createDailyLog(startOn, endOn, null, ETC, excludedFromFocusRate);
    }
}
