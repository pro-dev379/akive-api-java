package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ActivityTimeTest {

    @Test
    void 활동_시간_정보를_생성한다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(1);
        long focusTimeSec = 3_000L;

        ActivityTime activityTime = ActivityTime.of(startOn, endOn, focusTimeSec, true, NOW);

        assertThat(activityTime.getStartOn()).isEqualTo(startOn);
        assertThat(activityTime.getEndOn()).isEqualTo(endOn);
        assertThat(activityTime.getFocusTimeSec()).isEqualTo(focusTimeSec);
        assertThat(activityTime.isFocusRateExcluded()).isTrue();
    }

    @Test
    void 시작_시간과_종료_시간이_같고_실제_집중_시간이_0초인_활동_시간_정보를_생성한다() {
        ActivityTime activityTime = ActivityTime.of(NOW, NOW, 0L, false, NOW);

        assertThat(activityTime.durationSec()).isZero();
        assertThat(activityTime.getFocusTimeSec()).isZero();
    }

    @Test
    void 실제_집중_시간이_null인_활동_시간_정보를_생성한다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(1);

        ActivityTime activityTime = ActivityTime.of(startOn, endOn, null, false, NOW);

        assertThat(activityTime.getFocusTimeSec()).isNull();
    }

    @Test
    void 시작_시간이_종료_시간보다_이후면_활동_시간_정보를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.plusMinutes(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ActivityTime.of(startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_DATE_RANGE);
    }

    @Test
    void 시간_범위가_24시간을_초과하면_활동_시간_정보를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(24).minusNanos(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ActivityTime.of(startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_TOO_LONG);
    }

    @Test
    void 종료_시간이_미래이면_활동_시간_정보를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(1);
        LocalDateTime endOn = NOW.plusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ActivityTime.of(startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_IN_FUTURE);
    }

    @Test
    void 실제_집중_시간이_시간_범위보다_길면_활동_시간_정보를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(2);
        LocalDateTime endOn = NOW.minusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> ActivityTime.of(startOn, endOn, 3_601L, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
    }
}
