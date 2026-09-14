package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FocusTimeTest {

    private static final LocalDate LOG_ON = NOW.toLocalDate();

    @Test
    void 집중_시간_정보를_생성한다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(1);
        long focusTimeSec = 3_000L;

        FocusTime focusTime = FocusTime.of(LOG_ON, startOn, endOn, focusTimeSec, true, NOW);

        assertThat(focusTime.getLogOn()).isEqualTo(LOG_ON);
        assertThat(focusTime.getStartOn()).isEqualTo(startOn);
        assertThat(focusTime.getEndOn()).isEqualTo(endOn);
        assertThat(focusTime.getFocusTimeSec()).isEqualTo(focusTimeSec);
        assertThat(focusTime.isFocusRateExcluded()).isTrue();
    }

    @Test
    void 시작_시간과_종료_시간이_같고_실제_집중_시간이_0초인_집중_시간_정보를_생성한다() {
        FocusTime focusTime = FocusTime.of(LOG_ON, NOW, NOW, 0L, false, NOW);

        assertThat(focusTime.durationSec()).isZero();
        assertThat(focusTime.getFocusTimeSec()).isZero();
    }

    @Test
    void 실제_집중_시간이_null인_집중_시간_정보를_생성한다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(1);

        FocusTime focusTime = FocusTime.of(LOG_ON, startOn, endOn, null, false, NOW);

        assertThat(focusTime.getFocusTimeSec()).isNull();
    }

    @Test
    void 시작_시간이_종료_시간보다_이후면_집중_시간_정보를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.plusMinutes(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FocusTime.of(LOG_ON, startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_DATE_RANGE);
    }

    @Test
    void 시간_범위가_24시간을_초과하면_집중_시간_정보를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(24).minusNanos(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FocusTime.of(LOG_ON, startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_TOO_LONG);
    }

    @Test
    void 로그_날짜가_미래이면_집중_시간_정보를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(2);
        LocalDateTime endOn = NOW.minusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FocusTime.of(LOG_ON.plusDays(1), startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_DATE_IN_FUTURE);
    }

    @Test
    void 종료_시간이_미래이면_집중_시간_정보를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(1);
        LocalDateTime endOn = NOW.plusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FocusTime.of(LOG_ON, startOn, endOn, null, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_IN_FUTURE);
    }

    @Test
    void 실제_집중_시간이_시간_범위보다_길면_집중_시간_정보를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(2);
        LocalDateTime endOn = NOW.minusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> FocusTime.of(LOG_ON, startOn, endOn, 3_601L, false, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);
    }
}
