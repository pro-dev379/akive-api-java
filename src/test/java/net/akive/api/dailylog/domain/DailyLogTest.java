package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.dailylog.domain.DailyLogType.ATTENDANCE;
import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class DailyLogTest {

    @Test
    void 저장된_일일_로그를_복원한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );

        assertThat(dailyLog.getDailyLogId()).isEqualTo(DAILY_LOG_ID);
        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getFocusTime()).isEqualTo(FOCUS_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isFalse();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 일일_로그를_생성한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.create(
                user,
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );

        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getFocusTime()).isEqualTo(FOCUS_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isFalse();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 일일_로그를_수정한다() {
        DailyLog dailyLog = DailyLog.create(
                createUser(USER_ID),
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );

        dailyLog.update(
                TARGET_FOCUS_TIME,
                ATTENDANCE,
                TARGET_CONTENTS
        );

        assertThat(dailyLog.getLogOn()).isEqualTo(TARGET_LOG_ON);
        assertThat(dailyLog.getFocusTime()).isEqualTo(TARGET_FOCUS_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(ATTENDANCE);
        assertThat(dailyLog.getFocusTimeSec()).isNull();
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
        assertThat(dailyLog.getContents()).isEqualTo(TARGET_CONTENTS);
    }

    @Test
    void 같은_식별자를_가진_일일_로그는_같게_취급한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );
        DailyLog sameDailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                TARGET_FOCUS_TIME,
                ATTENDANCE,
                TARGET_CONTENTS
        );

        assertThat(dailyLog).isEqualTo(sameDailyLog);
        assertThat(dailyLog.hashCode()).isEqualTo(sameDailyLog.hashCode());
    }

    @Test
    void 식별자가_다른_일일_로그는_다르게_취급한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );
        DailyLog anotherDailyLog = DailyLog.reconstruct(
                ANOTHER_DAILY_LOG_ID,
                user,
                FOCUS_TIME,
                STUDY,
                CONTENTS
        );

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }

    @Test
    void 식별자가_없는_일일_로그끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.create(user, FOCUS_TIME, STUDY, CONTENTS);
        DailyLog anotherDailyLog = DailyLog.create(user, FOCUS_TIME, STUDY, CONTENTS);

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }
}
