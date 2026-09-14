package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.startOn;

public class FocusTimeFixture {

    public static FocusTime createFocusTime(LocalDate date, int startHour) {
        return createFocusTime(
                startOn(date, startHour),
                startOn(date, startHour + 1)
        );
    }

    public static FocusTime createFocusTime(LocalDateTime startOn, LocalDateTime endOn) {
        return FocusTime.of(
                startOn,
                endOn,
                between(startOn, endOn).toSeconds(),
                false,
                NOW
        );
    }
}
