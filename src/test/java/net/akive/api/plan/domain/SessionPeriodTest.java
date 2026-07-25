package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_SESSION_PERIOD;
import static net.akive.api.common.exception.business.BadRequestError.SESSION_PERIOD_TOO_LONG;
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
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(null, LocalDate.of(2026, 7, 14)))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 종료일이_null이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(LocalDate.of(2026, 7, 7), null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 시작일이_종료일과_같으면_세션_기간_생성에_실패한다() {
        // given
        LocalDate sessionOn = LocalDate.of(2026, 7, 7);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(sessionOn, sessionOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일이_종료일보다_이후이면_세션_기간_생성에_실패한다() {
        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(
                        LocalDate.of(2026, 7, 14),
                        LocalDate.of(2026, 7, 7)
                ))
                .extracting(BadRequestException::getError)
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
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2027, 7, 8)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(SESSION_PERIOD_TOO_LONG);
    }

    @Test
    void 시작일과_종료일이_같으면_같은_세션_기간이다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
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
                SessionWeek.of(1, LocalDate.of(2024, 3, 24), LocalDate.of(2024, 3, 24)),
                SessionWeek.of(2, LocalDate.of(2024, 3, 25), LocalDate.of(2024, 3, 31)),
                SessionWeek.of(3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 7)),
                SessionWeek.of(4, LocalDate.of(2024, 4, 8), LocalDate.of(2024, 4, 14)),
                SessionWeek.of(5, LocalDate.of(2024, 4, 15), LocalDate.of(2024, 4, 21)),
                SessionWeek.of(6, LocalDate.of(2024, 4, 22), LocalDate.of(2024, 4, 24))
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
                SessionWeek.of(1, LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 12))
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
                SessionWeek.of(1, LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12)),
                SessionWeek.of(2, LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 14))
        );
    }

    @Test
    void 주차_번호로_세션_주차를_조회한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2024, 3, 24),
                LocalDate.of(2024, 4, 24)
        );

        // when & then
        assertThat(sessionPeriod.findSessionWeek(1)).contains(
                SessionWeek.of(1, LocalDate.of(2024, 3, 24), LocalDate.of(2024, 3, 24))
        );
        assertThat(sessionPeriod.findSessionWeek(3)).contains(
                SessionWeek.of(3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 7))
        );
        assertThat(sessionPeriod.findSessionWeek(6)).contains(
                SessionWeek.of(6, LocalDate.of(2024, 4, 22), LocalDate.of(2024, 4, 24))
        );
    }

    @Test
    void 존재하지_않는_주차_번호이면_빈_값을_반환한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2024, 3, 24),
                LocalDate.of(2024, 4, 24)
        );

        // when & then
        assertThat(sessionPeriod.findSessionWeek(0)).isEmpty();
        assertThat(sessionPeriod.findSessionWeek(7)).isEmpty();
    }
}
