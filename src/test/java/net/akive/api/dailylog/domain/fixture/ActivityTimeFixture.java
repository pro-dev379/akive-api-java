package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.ActivityTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.CURRENT_INSTANT;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.startOn;

public class ActivityTimeFixture {

    private ActivityTimeFixture() {
    }

    public static ActivityTime createActivityTime(LocalDate date, int startHour) {
        return createActivityTime(
                startOn(date, startHour),
                startOn(date, startHour + 1)
        );
    }

    public static ActivityTime createActivityTime(LocalDateTime startOn, LocalDateTime endOn) {
        return createActivityTime(
                startOn,
                endOn,
                between(startOn, endOn).toSeconds(),
                false
        );
    }

    public static ActivityTime createActivityTime(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded
    ) {
        return ActivityTime.of(
                startOn,
                endOn,
                focusTimeSec,
                focusRateExcluded,
                CURRENT_INSTANT
        );
    }
}
