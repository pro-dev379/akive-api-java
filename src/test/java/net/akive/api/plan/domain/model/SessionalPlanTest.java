package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SessionalPlanTest {

    private static final Long SESSIONAL_PLAN_ID = 1L;
    private static final String SESSION_NAME = "2026 1월 학습";
    private static final LocalDateTime REQUESTED_AT =
            LocalDateTime.of(2025, 12, 31, 10, 0);

    @Test
    void 저장된_세셔널_플랜을_복원한다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        SessionalPlan sessionalPlan = SessionalPlan.reconstruct(
                SESSIONAL_PLAN_ID,
                user,
                SESSION_NAME,
                sessionPeriod
        );

        assertThat(sessionalPlan.getSessionalPlanId()).isEqualTo(SESSIONAL_PLAN_ID);
        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);
    }

    @Test
    void 세셔널_플랜을_생성한다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        SessionalPlan sessionalPlan = SessionalPlan.create(
                user,
                "  " + SESSION_NAME + "  ",
                sessionPeriod,
                REQUESTED_AT
        );

        assertThat(sessionalPlan.getSessionalPlanId()).isNull();
        assertThat(sessionalPlan.getUser()).isEqualTo(user);
        assertThat(sessionalPlan.getSessionName()).isEqualTo(SESSION_NAME);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(sessionPeriod);
    }

    @Test
    void 세셔널_플랜을_수정한다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        SessionalPlan sessionalPlan = SessionalPlan.reconstruct(
                SESSIONAL_PLAN_ID,
                user,
                SESSION_NAME,
                sessionPeriod
        );
        String updatedSessionName = "2026 2월 학습";
        SessionPeriod updatedSessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        );

        sessionalPlan.update(
                "  " + updatedSessionName + "  ",
                updatedSessionPeriod,
                REQUESTED_AT
        );

        assertThat(sessionalPlan.getSessionName()).isEqualTo(updatedSessionName);
        assertThat(sessionalPlan.getSessionPeriod()).isEqualTo(updatedSessionPeriod);
    }

    @Test
    void 시작일이_요청일보다_과거면_생성할_수_없다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                REQUESTED_AT.toLocalDate().minusDays(1),
                REQUESTED_AT.toLocalDate().plusDays(1)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(user, SESSION_NAME, sessionPeriod, REQUESTED_AT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 종료일이_요청일과_같으면_생성할_수_없다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                REQUESTED_AT.toLocalDate(),
                REQUESTED_AT.toLocalDate().plusDays(1)
        );
        LocalDateTime requestedAt = REQUESTED_AT.plusDays(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(user, SESSION_NAME, sessionPeriod, requestedAt))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_SESSION_PERIOD);
    }

    @Test
    void 공백_이름으로는_생성할_수_없다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(user, "   ", sessionPeriod, REQUESTED_AT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 이름이_최대_길이를_초과하면_생성할_수_없다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        String tooLongSessionName = "a".repeat(SESSION_NAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> SessionalPlan.create(user, tooLongSessionName, sessionPeriod, REQUESTED_AT))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 같은_식별자를_가진_세셔널_플랜은_같게_취급한다() {
        SessionalPlan sessionalPlan = createSessionalPlan(1L);
        SessionalPlan sameSessionalPlan = createSessionalPlan(1L);

        assertThat(sessionalPlan).isEqualTo(sameSessionalPlan);
        assertThat(sessionalPlan.hashCode()).isEqualTo(sameSessionalPlan.hashCode());
    }

    @Test
    void 식별자가_다른_세셔널_플랜은_다르게_취급한다() {
        SessionalPlan sessionalPlan = createSessionalPlan(1L);
        SessionalPlan anotherSessionalPlan = createSessionalPlan(2L);

        assertThat(sessionalPlan).isNotEqualTo(anotherSessionalPlan);
    }

    @Test
    void 식별자가_없는_세셔널_플랜끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createPersistedUser(1L);
        SessionPeriod sessionPeriod = new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        SessionalPlan sessionalPlan = SessionalPlan.create(user, SESSION_NAME, sessionPeriod, REQUESTED_AT);
        SessionalPlan anotherSessionalPlan = SessionalPlan.create(user, SESSION_NAME, sessionPeriod, REQUESTED_AT);

        assertThat(sessionalPlan).isNotEqualTo(anotherSessionalPlan);
    }

    private SessionalPlan createSessionalPlan(Long sessionalPlanId) {
        return SessionalPlan.reconstruct(
                sessionalPlanId,
                createPersistedUser(1L),
                SESSION_NAME,
                new SessionPeriod(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                )
        );
    }
}
