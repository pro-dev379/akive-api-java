package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.akive.api.user.domain.UserFixture.createReconstructedUser;

public class SessionalPlanFixture {

    public static final Long SESSIONAL_PLAN_ID = 10L;
    public static final Long UNKNOWN_SESSIONAL_PLAN_ID = Long.MAX_VALUE;
    public static final Long ANOTHER_SESSIONAL_PLAN_ID = 11L;
    public static final int FIRST_WEEK_NUM = 1;
    public static final int UNKNOWN_WEEK_NUM = 999;
    public static final String SESSION_NAME = "2026 1월 학습";
    public static final LocalDateTime REQUESTED_AT =
            LocalDateTime.of(2025, 12, 31, 10, 0);
    public static final SessionPeriod SESSION_PERIOD = new SessionPeriod(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 12)
    );
    public static final String TARGET_SESSION_NAME = "2026 2월 학습";
    public static final SessionPeriod TARGET_SESSION_PERIOD = new SessionPeriod(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 12)
    );
    public static final SessionPeriod CHANGED_SESSION_PERIOD = new SessionPeriod(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 13)
    );

    private SessionalPlanFixture() {
    }

    public static SessionalPlan createSessionalPlan() {
        return createSessionalPlan(createReconstructedUser());
    }

    public static SessionalPlan createSessionalPlan(User user) {
        return createSessionalPlan(user, SESSION_NAME);
    }

    public static SessionalPlan createSessionalPlan(User user, LocalDateTime requestedAt) {
        return createSessionalPlan(user, SESSION_NAME, requestedAt);
    }

    public static SessionalPlan createSessionalPlan(User user, String sessionName) {
        return createSessionalPlan(user, sessionName, REQUESTED_AT);
    }

    public static SessionalPlan createSessionalPlan(User user, String sessionName, LocalDateTime requestedAt) {
        return SessionalPlan.create(user, sessionName, SESSION_PERIOD, requestedAt);
    }

    public static SessionalPlan createReconstructedSessionalPlan() {
        return createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, createReconstructedUser());
    }

    public static SessionalPlan createReconstructedSessionalPlan(Long sessionalPlanId) {
        return createReconstructedSessionalPlan(sessionalPlanId, createReconstructedUser(), SESSION_NAME, SESSION_PERIOD);
    }

    public static SessionalPlan createReconstructedSessionalPlan(Long sessionalPlanId, User user) {
        return createReconstructedSessionalPlan(sessionalPlanId, user, SESSION_NAME, SESSION_PERIOD);
    }

    public static SessionalPlan createReconstructedSessionalPlan(Long sessionalPlanId, SessionalPlan sessionalPlan) {
        return createReconstructedSessionalPlan(
                sessionalPlanId,
                sessionalPlan.getUser(),
                sessionalPlan.getSessionName(),
                sessionalPlan.getSessionPeriod()
        );
    }

    public static SessionalPlan createReconstructedSessionalPlan(
            Long sessionalPlanId,
            User user,
            String sessionName,
            SessionPeriod sessionPeriod
    ) {
        return SessionalPlan.reconstruct(
                sessionalPlanId,
                user,
                sessionName,
                sessionPeriod
        );
    }
}
