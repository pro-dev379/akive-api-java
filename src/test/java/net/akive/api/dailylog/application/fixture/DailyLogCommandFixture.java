package net.akive.api.dailylog.application.fixture;

import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;

import static net.akive.api.dailylog.domain.DailyLogType.ATTENDANCE;
import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.ActivityTimeFixture.createActivityTime;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class DailyLogCommandFixture {

    private DailyLogCommandFixture() {
    }

    public static DailyLogCreateCommand createDailyLogCreateCommand() {
        return createDailyLogCreateCommand(USER_ID);
    }

    public static DailyLogCreateCommand createDailyLogCreateCommand(Long userId) {
        return new DailyLogCreateCommand(
                userId,
                ACTIVITY_TIME,
                STUDY,
                CONTENTS,
                CURRENT_INSTANT
        );
    }

    public static DailyLogUpdateCommand createDailyLogUpdateCommand() {
        return new DailyLogUpdateCommand(
                DAILY_LOG_ID,
                USER_ID,
                createActivityTime(TARGET_START_ON, TARGET_END_ON, null, true),
                ATTENDANCE,
                TARGET_CONTENTS,
                CURRENT_INSTANT
        );
    }
}
