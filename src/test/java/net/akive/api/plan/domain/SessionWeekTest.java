package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionWeekTest {

    @Test
    void 세션_주차를_생성한다() {
        // given
        LocalDate startOn = LocalDate.of(2026, 7, 7);
        LocalDate endOn = LocalDate.of(2026, 7, 13);

        // when
        SessionWeek sessionWeek = SessionWeek.of(1, startOn, endOn);

        // then
        assertThat(sessionWeek.weekNum()).isEqualTo(1);
        assertThat(sessionWeek.weekPeriod()).isEqualTo(new WeekPeriod(startOn, endOn));
    }

    @Test
    void 주차_번호가_양수가_아니면_세션_주차_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> SessionWeek.of(0, LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 13)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 주간_기간이_null이면_세션_주차_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new SessionWeek(1, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }
}
