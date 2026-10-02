package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;

import java.time.Instant;
import java.time.LocalDate;

import static net.akive.api.common.time.ServiceDayPolicy.toInstantAtServiceZone;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;

public class SessionalPlanFixture {

    public static final Long SESSIONAL_PLAN_ID = 10L;
    public static final Long UNKNOWN_SESSIONAL_PLAN_ID = Long.MAX_VALUE;
    public static final Long ANOTHER_SESSIONAL_PLAN_ID = 11L;
    public static final int FIRST_WEEK_NUM = 1;
    public static final int UNKNOWN_WEEK_NUM = 999;
    public static final String SESSION_NAME = "2026 1월 학습";
    public static final String ANOTHER_SESSION_NAME = "2026 2월 학습";
    public static final LocalDate CURRENT_KOREAN_DATE = LocalDate.of(2025, 12, 31);
    public static final Instant CURRENT_INSTANT =
            toInstantAtServiceZone(CURRENT_KOREAN_DATE.atTime(10, 0));
    public static final SessionPeriod SESSION_PERIOD = new SessionPeriod(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 12)
    );
    public static final String TARGET_SESSION_NAME = "업데이트 2026 2월 학습";
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

    public static SessionalPlan createSessionalPlan(User user, LocalDate currentDate) {
        return createSessionalPlan(user, SESSION_NAME, currentDate);
    }

    public static SessionalPlan createSessionalPlan(User user, String sessionName) {
        return createSessionalPlan(user, sessionName, CURRENT_KOREAN_DATE);
    }

    public static SessionalPlan createSessionalPlan(User user, String sessionName, LocalDate currentDate) {
        return SessionalPlan.create(user, sessionName, SESSION_PERIOD, currentDate);
    }

    public static SessionalPlan createReconstructedSessionalPlan() {
        return createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, createReconstructedUser());
    }

    public static SessionalPlan createReconstructedSessionalPlan(Long sessionalPlanId) {
        return createReconstructedSessionalPlan(
                sessionalPlanId,
                createReconstructedUser(),
                SESSION_NAME,
                SESSION_PERIOD
        );
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
