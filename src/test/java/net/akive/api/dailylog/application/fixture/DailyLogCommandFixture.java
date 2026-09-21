package net.akive.api.dailylog.application.fixture;

import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;

import static net.akive.api.dailylog.domain.DailyLogType.ATTENDANCE;
import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
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
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );
    }

    public static DailyLogUpdateCommand createDailyLogUpdateCommand() {
        return new DailyLogUpdateCommand(
                DAILY_LOG_ID,
                USER_ID,
                TARGET_FOCUS_TIME,
                ATTENDANCE,
                TARGET_CONTENTS,
                NOW
        );
    }
}
