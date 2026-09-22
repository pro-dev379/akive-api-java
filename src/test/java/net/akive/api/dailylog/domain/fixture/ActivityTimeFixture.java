package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.ActivityTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
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
        return ActivityTime.of(
                startOn,
                endOn,
                between(startOn, endOn).toSeconds(),
                false,
                NOW
        );
    }
}
