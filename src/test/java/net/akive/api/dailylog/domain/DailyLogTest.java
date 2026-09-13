package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.VALUE_NEGATIVE;
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
                TIME_RANGE,
                STUDY,
                FOCUS_TIME_SEC,
                true,
                CONTENTS
        );

        assertThat(dailyLog.getDailyLogId()).isEqualTo(DAILY_LOG_ID);
        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getTimeRange()).isEqualTo(TIME_RANGE);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 일일_로그를_생성한다() {
        User user = createUser(USER_ID);
        DailyLog dailyLog = DailyLog.create(
                user,
                LOG_ON,
                TIME_RANGE,
                STUDY,
                FOCUS_TIME_SEC,
                false,
                CONTENTS
        );

        assertThat(dailyLog.getUser()).isEqualTo(user);
        assertThat(dailyLog.getLogOn()).isEqualTo(LOG_ON);
        assertThat(dailyLog.getTimeRange()).isEqualTo(TIME_RANGE);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(STUDY);
        assertThat(dailyLog.getFocusTimeSec()).isEqualTo(FOCUS_TIME_SEC);
        assertThat(dailyLog.isFocusRateExcluded()).isFalse();
        assertThat(dailyLog.getContents()).isEqualTo(CONTENTS);
    }

    @Test
    void 시작_시간과_종료_시간이_같고_집중_시간이_0초인_일일_로그를_생성한다() {
        DailyLog dailyLog = DailyLog.create(
                createUser(USER_ID),
                LOG_ON,
                DailyLogTimeRange.of(START_ON, START_ON, NOW),
                STUDY,
                0L,
                false,
                CONTENTS
        );

        assertThat(DailyLog.calculateDailyLogDurationSec(dailyLog.getTimeRange())).isZero();
        assertThat(dailyLog.getFocusTimeSec()).isZero();
    }

    @Test
    void 집중_시간이_null인_일일_로그를_생성한다() {
        DailyLog dailyLog = DailyLog.create(
                createUser(USER_ID),
                LOG_ON,
                TIME_RANGE,
                STUDY,
                null,
                false,
                CONTENTS
        );

        assertThat(dailyLog.getFocusTimeSec()).isNull();
    }

    @Test
    void 집중_시간이_일일_로그_시간보다_길면_데일리_로그를_생성할_수_없다() {
        LocalDateTime startOn = LocalDateTime.of(2026, 9, 12, 10, 0);
        LocalDateTime endOn = startOn.plusHours(1);
        DailyLogTimeRange timeRange = DailyLogTimeRange.of(startOn, endOn, NOW);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.create(
                        createUser(USER_ID),
                        LOG_ON,
                        timeRange,
                        STUDY,
                        3_601L,
                        false,
                        CONTENTS
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
    }

    @Test
    void 집중_시간이_음수이면_생성할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLog.create(
                        createUser(USER_ID),
                        LOG_ON,
                        TIME_RANGE,
                        STUDY,
                        -1L,
                        false,
                        CONTENTS
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 일일_로그를_수정한다() {
        DailyLog dailyLog = DailyLog.create(
                createUser(USER_ID),
                LOG_ON,
                TIME_RANGE,
                STUDY,
                FOCUS_TIME_SEC,
                false,
                CONTENTS
        );

        dailyLog.update(
                TARGET_LOG_ON,
                TARGET_TIME_RANGE,
                ATTENDANCE,
                null,
                true,
                TARGET_CONTENTS
        );

        assertThat(dailyLog.getLogOn()).isEqualTo(TARGET_LOG_ON);
        assertThat(dailyLog.getTimeRange()).isEqualTo(TARGET_TIME_RANGE);
        assertThat(dailyLog.getDailyLogType()).isEqualTo(ATTENDANCE);
        assertThat(dailyLog.getFocusTimeSec()).isNull();
        assertThat(dailyLog.isFocusRateExcluded()).isTrue();
        assertThat(dailyLog.getContents()).isEqualTo(TARGET_CONTENTS);
    }
}
