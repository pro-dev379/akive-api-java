package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionalPlanTest {

    @Test
    void 회원의_세셔널_플랜을_생성한다() {
        // given
        Long userId = 1L;
        String sessionName = "1회차";
        LocalDate sessionStartOn = LocalDate.of(2026, 7, 7);
        LocalDate sessionEndOn = LocalDate.of(2026, 7, 14);
        SessionPeriod sessionPeriod = new SessionPeriod(sessionStartOn, sessionEndOn);
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                userId,
                sessionName,
                sessionPeriod,
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isNull();
        assertThat(sessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(sessionName);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);
    }

    @Test
    void 세셔널_플랜명_앞뒤_공백을_제거해서_세셔널_플랜을_생성한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                1L,
                "  1회차  ",
                sessionPeriod,
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionName()).isEqualTo("1회차");
    }

    @Test
    void 세셔널_플랜명이_공백이면_생성에_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        1L,
                        "   ",
                        sessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }
    
    @Test
    void 세셔널_플랜명은_50자를_초과하면_생성에_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        1L,
                        "가".repeat(51),
                        sessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세셔널_플랜명은_50자일_수_있다() {
        // given
        String sessionName = "가".repeat(50);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                1L,
                sessionName,
                sessionPeriod,
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionName()).isEqualTo(sessionName);
    }

    @Test
    void 시작일이_요청일보다_이전이면_세셔널_플랜_생성에_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 7)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        1L,
                        "1회차",
                        sessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 종료일이_요청일과_같으면_세셔널_플랜_생성에_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 6)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        1L,
                        "1회차",
                        sessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 영속화된_세셔널_플랜을_도메인_객체로_복원한다() {
        // given
        Long persistedSessionalPlanId = 1L;
        Long persistedUserId = 1L;
        String persistedSessionName = "1회차";
        LocalDate persistedSessionStartOn = LocalDate.of(2026, 7, 1);
        LocalDate persistedSessionEndOn = LocalDate.of(2026, 7, 7);
        SessionPeriod persistedSessionPeriod = new SessionPeriod(persistedSessionStartOn, persistedSessionEndOn);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.create(
                persistedSessionalPlanId,
                persistedUserId,
                persistedSessionName,
                persistedSessionPeriod
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(persistedSessionalPlanId);
        assertThat(sessionalPlan.getUserId()).isEqualTo(persistedUserId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(persistedSessionName);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(persistedSessionPeriod);
    }

    @Test
    void 영속화된_세셔널_플랜_복원_시_세셔널_플랜명_앞뒤_공백을_제거한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        // when
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                1L,
                "  1회차  ",
                sessionPeriod
        );

        // then
        assertThat(sessionalPlan.getSessionName()).isEqualTo("1회차");
    }

    @Test
    void 영속화된_세셔널_플랜_복원_시_세셔널_플랜명이_공백이면_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.create(
                        1L,
                        1L,
                        "   ",
                        sessionPeriod
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 영속화된_세셔널_플랜_복원_시_세셔널_플랜명이_50자를_초과하면_실패한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 7)
        );

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> SessionalPlan.create(
                        1L,
                        1L,
                        "가".repeat(51),
                        sessionPeriod
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        SessionPeriod updatedSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 15)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                1L,
                "1회차",
                sessionPeriod
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                "수정된 1회차",
                updatedSessionPeriod,
                requestedAt
        );

        // then
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(1L);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(1L);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(updatedSessionPeriod);
    }

    @Test
    void 기간을_변경하지_않고_세셔널_플랜명만_수정한다() {
        // given
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                1L,
                "1회차",
                sessionPeriod
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                "수정된 1회차",
                sessionPeriod,
                requestedAt
        );

        // then
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);
    }

    @Test
    void 세셔널_플랜명_앞뒤_공백을_제거해서_수정한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                "  수정된 1회차  ",
                sessionalPlan.getSessionPeriod(),
                requestedAt
        );

        // then
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
    }

    @Test
    void 세셔널_플랜명은_50자를_초과하면_수정에_실패한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> sessionalPlan.update(
                        "가".repeat(51),
                        sessionalPlan.getSessionPeriod(),
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 세셔널_플랜명이_공백이면_수정에_실패한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> sessionalPlan.update(
                        "   ",
                        sessionalPlan.getSessionPeriod(),
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 시작일이_요청일보다_이전이면_세셔널_플랜_수정에_실패한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();
        SessionPeriod requestedSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 7)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> sessionalPlan.update(
                        "수정된 1회차",
                        requestedSessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 종료일이_요청일과_같으면_세셔널_플랜_수정에_실패한다() {
        // given
        SessionalPlan sessionalPlan = sessionalPlan();
        SessionPeriod requestedSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 6)
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> sessionalPlan.update(
                        "수정된 1회차",
                        requestedSessionPeriod,
                        requestedAt
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    private SessionalPlan sessionalPlan() {
        return SessionalPlan.create(
                1L,
                1L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
