package net.akive.api.dailylog.domain.fixture;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.user.domain.User;

import java.time.LocalDateTime;

import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;

public class TimeUsageRatesFixture {

    private TimeUsageRatesFixture() {
    }

    public static DailyLog log(LocalDateTime startOn, LocalDateTime endOn, DailyLogType type) {
        User user = createReconstructedUser();

        ActivityTime activityTime = ActivityTime.of(startOn, endOn, null, false, NOW);
        return DailyLog.create(user, activityTime, type, type.getLabel(), NOW);
    }
}
