package net.akive.api.plan.domain;

import net.akive.api.plan.domain.exception.PlanException;
import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.plan.domain.exception.PlanErrorCode.INVALID_SESSION_PERIOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionalPlanTest {

    @Test
    void 회원의_세셔널_플랜을_생성한다() {
        // given
        Long userId = 2L;
        String sessionName = "1회차";
        LocalDate sessionStartOn = LocalDate.of(2026, 7, 7);
        LocalDate sessionEndOn = LocalDate.of(2026, 7, 14);
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                userId,
                sessionName,
                new SessionPeriod(sessionStartOn, sessionEndOn),
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isNull();
        assertThat(sessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(sessionName);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(new SessionPeriod(sessionStartOn, sessionEndOn));
    }

    @Test
    void 세셔널_플랜명_앞뒤_공백을_제거해서_세셔널_플랜을_생성한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.createForUser(
                2L,
                "  1회차  ",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                ),
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionName()).isEqualTo("1회차");
    }

    @Test
    void 세셔널_플랜명이_공백이면_생성에_실패한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                "   ",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                ),
                requestedAt
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 시작일이_요청일보다_이전이면_세셔널_플랜_생성에_실패한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 5),
                        LocalDate.of(2026, 7, 7)
                ),
                requestedAt
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 종료일이_요청일과_같으면_세셔널_플랜_생성에_실패한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 5),
                        LocalDate.of(2026, 7, 6)
                ),
                requestedAt
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 영속화된_세셔널_플랜을_도메인_객체로_복원한다() {
        // given
        Long persistedSessionalPlanId = 1L;
        Long persistedUserId = 2L;
        String persistedSessionName = "1회차";
        LocalDate persistedSessionStartOn = LocalDate.of(2026, 7, 1);
        LocalDate persistedSessionEndOn = LocalDate.of(2026, 7, 7);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.create(
                persistedSessionalPlanId,
                persistedUserId,
                persistedSessionName,
                new SessionPeriod(persistedSessionStartOn, persistedSessionEndOn)
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(persistedSessionalPlanId);
        assertThat(sessionalPlan.getUserId()).isEqualTo(persistedUserId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(persistedSessionName);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(
                new SessionPeriod(persistedSessionStartOn, persistedSessionEndOn)
        );
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        // given
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                2L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                "수정된 1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                ),
                requestedAt
        );

        // then
        assertThat(updatedSessionalPlan.getSessionalPlanId()).isEqualTo(1L);
        assertThat(updatedSessionalPlan.getUserId()).isEqualTo(2L);
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 15)
                )
        );
    }

    @Test
    void 기간을_변경하지_않고_세셔널_플랜명만_수정한다() {
        // given
        SessionalPlan sessionalPlan = SessionalPlan.create(
                1L,
                2L,
                "1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when
        SessionalPlan updatedSessionalPlan = sessionalPlan.update(
                "수정된 1회차",
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                ),
                requestedAt
        );

        // then
        assertThat(updatedSessionalPlan.getSessionName()).isEqualTo("수정된 1회차");
        assertThat(updatedSessionalPlan.getSessionPeriod()).isEqualTo(
                new SessionPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
