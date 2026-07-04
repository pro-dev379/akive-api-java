package net.akive.api.plan.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SessionalPlanTest {

    @Test
    void 세셔널_플랜을_생성한다() {
        // given
        Long sessionalPlanId = 1L;
        Long userId = 2L;
        String sessionName = "1회차";
        LocalDate sessionStartOn = LocalDate.of(2026, 7, 1);
        LocalDate sessionEndOn = LocalDate.of(2026, 7, 7);

        // when
        SessionalPlan sessionalPlan = SessionalPlan.create(
                sessionalPlanId,
                userId,
                sessionName,
                sessionStartOn,
                sessionEndOn
        );

        // then
        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(sessionalPlanId);
        assertThat(sessionalPlan.getUserId()).isEqualTo(userId);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(sessionName);
        assertThat(sessionalPlan.getSessionStartOn()).isEqualTo(sessionStartOn);
        assertThat(sessionalPlan.getSessionEndOn()).isEqualTo(sessionEndOn);
    }
}
