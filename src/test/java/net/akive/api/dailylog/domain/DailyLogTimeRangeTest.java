package net.akive.api.dailylog.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyLogTimeRangeTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 13, 12, 0);

    @Test
    void 일일_로그_시간_범위를_생성한다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(1);

        DailyLogTimeRange timeRange = DailyLogTimeRange.of(startOn, endOn, NOW);

        assertThat(timeRange.getStartOn()).isEqualTo(startOn);
        assertThat(timeRange.getEndOn()).isEqualTo(endOn);
    }

    @Test
    void 시작_시간이_종료_시간보다_이후면_일일_로그_시간_범위를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.plusMinutes(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLogTimeRange.of(startOn, endOn, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_DATE_RANGE);
    }

    @Test
    void 시간_범위가_24시간을_초과하면_일일_로그_시간_범위를_생성할_수_없다() {
        LocalDateTime endOn = NOW.minusHours(1);
        LocalDateTime startOn = endOn.minusHours(24).minusNanos(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLogTimeRange.of(startOn, endOn, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_TOO_LONG);
    }

    @Test
    void 종료_시간이_미래이면_일일_로그_시간_범위를_생성할_수_없다() {
        LocalDateTime startOn = NOW.minusHours(1);
        LocalDateTime endOn = NOW.plusHours(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> DailyLogTimeRange.of(startOn, endOn, NOW))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_LOG_TIME_RANGE_IN_FUTURE);
    }
}
