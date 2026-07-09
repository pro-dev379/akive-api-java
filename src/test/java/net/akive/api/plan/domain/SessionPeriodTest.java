package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.INVALID_SESSION_PERIOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionPeriodTest {

    @Test
    void 세션_기간을_생성한다() {
        // given
        LocalDate startOn = LocalDate.of(2026, 7, 7);
        LocalDate endOn = LocalDate.of(2026, 7, 14);

        // when
        SessionPeriod sessionPeriod = SessionPeriod.of(startOn, endOn);

        // then
        assertThat(sessionPeriod.startOn()).isEqualTo(startOn);
        assertThat(sessionPeriod.endOn()).isEqualTo(endOn);
    }

    @Test
    void 시작일이_null이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> SessionPeriod.of(null, LocalDate.of(2026, 7, 14)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 종료일이_null이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> SessionPeriod.of(LocalDate.of(2026, 7, 7), null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 시작일이_종료일과_같으면_세션_기간_생성에_실패한다() {
        // given
        LocalDate sessionOn = LocalDate.of(2026, 7, 7);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> SessionPeriod.of(sessionOn, sessionOn))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일이_종료일보다_이후이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> SessionPeriod.of(
                        LocalDate.of(2026, 7, 14),
                        LocalDate.of(2026, 7, 7)
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일과_종료일이_같으면_같은_세션_기간이다() {
        // given
        SessionPeriod sessionPeriod =
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                );

        // when & then
        assertThat(sessionPeriod).isNotEqualTo(
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
        assertThat(sessionPeriod).isEqualTo(
                SessionPeriod.of(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
