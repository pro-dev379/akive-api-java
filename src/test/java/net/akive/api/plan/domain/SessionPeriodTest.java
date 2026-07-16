package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.INVALID_SESSION_PERIOD;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_TOO_LONG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionPeriodTest {

    @Test
    void 세션_기간을_생성한다() {
        // given
        LocalDate startOn = LocalDate.of(2026, 7, 7);
        LocalDate endOn = LocalDate.of(2026, 7, 14);

        // when
        SessionPeriod sessionPeriod = new SessionPeriod(startOn, endOn);

        // then
        assertThat(sessionPeriod.startOn()).isEqualTo(startOn);
        assertThat(sessionPeriod.endOn()).isEqualTo(endOn);
    }

    @Test
    void 시작일이_null이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new SessionPeriod(null, LocalDate.of(2026, 7, 14)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 종료일이_null이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new SessionPeriod(LocalDate.of(2026, 7, 7), null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 시작일이_종료일과_같으면_세션_기간_생성에_실패한다() {
        // given
        LocalDate sessionOn = LocalDate.of(2026, 7, 7);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> new SessionPeriod(sessionOn, sessionOn))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일이_종료일보다_이후이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> new SessionPeriod(
                        LocalDate.of(2026, 7, 14),
                        LocalDate.of(2026, 7, 7)
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 세션_기간이_12개월이면_세션_기간을_생성한다() {
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2027, 7, 7)
        );

        assertThat(sessionPeriod.startOn()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(sessionPeriod.endOn()).isEqualTo(LocalDate.of(2027, 7, 7));
    }

    @Test
    void 세션_기간이_12개월을_초과하면_세션_기간_생성에_실패한다() {
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2027, 7, 8)
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(SESSION_PERIOD_TOO_LONG);
    }

    @Test
    void 시작일과_종료일이_같으면_같은_세션_기간이다() {
        // given
        SessionPeriod sessionPeriod =
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                );

        // when & then
        assertThat(sessionPeriod).isNotEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
        assertThat(sessionPeriod).isEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }

    @Test
    void 세션_기간을_주차별_기간으로_나눈다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2024, 3, 24),
                LocalDate.of(2024, 4, 24)
        );

        // when
        List<SessionWeek> sessionWeeks = sessionPeriod.splitIntoWeeks();

        // then
        assertThat(sessionWeeks).containsExactly(
                new SessionWeek(
                        1,
                        new WeekPeriod(LocalDate.of(2024, 3, 24), LocalDate.of(2024, 3, 24))
                ),
                new SessionWeek(
                        2,
                        new WeekPeriod(LocalDate.of(2024, 3, 25), LocalDate.of(2024, 3, 31))
                ),
                new SessionWeek(
                        3,
                        new WeekPeriod(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 7))
                ),
                new SessionWeek(
                        4,
                        new WeekPeriod(LocalDate.of(2024, 4, 8), LocalDate.of(2024, 4, 14))
                ),
                new SessionWeek(
                        5,
                        new WeekPeriod(LocalDate.of(2024, 4, 15), LocalDate.of(2024, 4, 21))
                ),
                new SessionWeek(
                        6,
                        new WeekPeriod(LocalDate.of(2024, 4, 22), LocalDate.of(2024, 4, 24))
                )
        );
    }

    @Test
    void 한_주의_시작일이_월요일이_아니더라도_하나의_주차로_인정한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 12)
        );

        // when
        List<SessionWeek> sessionWeeks = sessionPeriod.splitIntoWeeks();

        // then
        assertThat(sessionWeeks).containsExactly(
                new SessionWeek(
                        1,
                        new WeekPeriod(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 12))
                )
        );
    }

    @Test
    void 한_주의_종료일이_일요일이_아니더라도_하나의_주차로_인정한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        );

        // when
        List<SessionWeek> sessionWeeks = sessionPeriod.splitIntoWeeks();

        // then
        assertThat(sessionWeeks).containsExactly(
                new SessionWeek(
                        1,
                        new WeekPeriod(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12))
                ),
                new SessionWeek(
                        2,
                        new WeekPeriod(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
                )
        );
    }
}
