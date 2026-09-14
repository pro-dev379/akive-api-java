package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_DATE_IN_FUTURE;
import static net.akive.api.dailylog.domain.DailyLogType.ATTENDANCE;
import static net.akive.api.dailylog.domain.DailyLogType.STUDY;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyLogTest {

    @Test
    void 저장된_일일_로그를_복원한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
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
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
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
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );

        dailyLog.update(
                TARGET_LOG_ON,
                TARGET_FOCUS_TIME,
                ATTENDANCE,
                TARGET_CONTENTS,
                NOW
        );

        assertThat(dailyLog.getLogOn()).isEqualTo(TARGET_LOG_ON);
        assertThat(dailyLog.getFocusTime()).isEqualTo(TARGET_FOCUS_TIME);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(ATTENDANCE);
        assertThat(dailyLog.getFocusTimeSec()).isNull();
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
        assertThat(dailyLog.getContents()).isEqualTo(TARGET_CONTENTS);
    }

    @Test
    void 로그_날짜가_미래이면_일일_로그를_생성할_수_없다() {
        User user = createUser(USER_ID);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.create(
                        user,
                        NOW.toLocalDate().plusDays(1),
                        FOCUS_TIME,
                        STUDY,
                        CONTENTS,
                        NOW
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_DATE_IN_FUTURE);
    }

    @Test
    void 같은_식별자를_가진_일일_로그는_같게_취급한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );
        DailyLog sameDailyLog = DailyLog.reconstruct(
                DAILY_LOG_ID,
                user,
                TARGET_LOG_ON,
                TARGET_FOCUS_TIME,
                ATTENDANCE,
                TARGET_CONTENTS,
                NOW
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
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );
        DailyLog anotherDailyLog = DailyLog.reconstruct(
                ANOTHER_DAILY_LOG_ID,
                user,
                LOG_ON,
                FOCUS_TIME,
                STUDY,
                CONTENTS,
                NOW
        );

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }

    @Test
    void 식별자가_없는_일일_로그끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.create(user, LOG_ON, FOCUS_TIME, STUDY, CONTENTS, NOW);
        DailyLog anotherDailyLog = DailyLog.create(user, LOG_ON, FOCUS_TIME, STUDY, CONTENTS, NOW);

        assertThat(dailyLog).isNotEqualTo(anotherDailyLog);
    }
}
