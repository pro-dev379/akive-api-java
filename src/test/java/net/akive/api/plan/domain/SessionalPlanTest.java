package net.akive.api.plan.domain;

import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.INVALID_SESSION_PERIOD;
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
                sessionStartOn,
                sessionEndOn,
                requestedAt
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isNull();
        assertThat(sessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(sessionName);
        assertThat(sessionalPlan.getSessionStartOn()).isEqualTo(sessionStartOn);
        assertThat(sessionalPlan.getSessionEndOn()).isEqualTo(sessionEndOn);
    }

    @Test
    void 시작일이_종료일과_같으면_세셔널_플랜_생성에_실패한다() {
        // given
        LocalDate sessionOn = LocalDate.of(2026, 7, 7);
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                        "1회차",
                        sessionOn,
                        sessionOn,
                        requestedAt
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 시작일이_요청일보다_이전이면_세셔널_플랜_생성에_실패한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                        "1회차",
                        LocalDate.of(2026, 7, 5),
                        LocalDate.of(2026, 7, 7),
                        requestedAt
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 종료일이_요청일과_같으면_세셔널_플랜_생성에_실패한다() {
        // given
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 6, 12, 0);

        // when & then
        assertThatExceptionOfType(SessionalPlanException.class)
                .isThrownBy(() -> SessionalPlan.createForUser(
                        2L,
                        "1회차",
                        LocalDate.of(2026, 7, 5),
                        LocalDate.of(2026, 7, 6),
                        requestedAt
                ))
                .extracting(SessionalPlanException::getErrorCode)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 저장된_세셔널_플랜_값으로_도메인_객체를_생성한다() {
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
                persistedSessionStartOn,
                persistedSessionEndOn
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(persistedSessionalPlanId);
        assertThat(sessionalPlan.getUserId()).isEqualTo(persistedUserId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(persistedSessionName);
        assertThat(sessionalPlan.getSessionStartOn()).isEqualTo(persistedSessionStartOn);
        assertThat(sessionalPlan.getSessionEndOn()).isEqualTo(persistedSessionEndOn);
    }
}
