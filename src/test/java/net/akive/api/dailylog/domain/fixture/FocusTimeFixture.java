package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.startOn;

public class FocusTimeFixture {

    public static FocusTime createFocusTime(LocalDate logOn, int startHour) {
        return createFocusTime(
                logOn,
                startOn(logOn, startHour),
                startOn(logOn, startHour + 1)
        );
    }

    public static FocusTime createFocusTime(LocalDateTime startOn, LocalDateTime endOn) {
        return createFocusTime(startOn.toLocalDate(), startOn, endOn);
    }

    public static FocusTime createFocusTime(LocalDate logOn, LocalDateTime startOn, LocalDateTime endOn) {
        return FocusTime.of(
                logOn,
                startOn,
                endOn,
                between(startOn, endOn).toSeconds(),
                false,
                NOW
        );
    }
}
