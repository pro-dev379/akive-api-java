package net.akive.api.dailylog.application.fixture;

import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;

import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.CONTENTS;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class DailyLogCommandFixture {

    public static DailyLogCreateCommand createDailyLogCreateCommand() {
        return createDailyLogCreateCommand(USER_ID);
    }

    public static DailyLogCreateCommand createDailyLogCreateCommand(Long userId) {
        return new DailyLogCreateCommand(
                userId,
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );
    }
}
