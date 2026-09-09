package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.domain.value.SessionPeriod;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.user.domain.UserFixture.USER_ID;

public class SessionalPlanCommandFixture {

    private SessionalPlanCommandFixture() {
    }

    public static SessionalPlanCreateCommand createSessionalPlanCreateCommand() {
        return createSessionalPlanCreateCommand(USER_ID);
    }

    public static SessionalPlanCreateCommand createSessionalPlanCreateCommand(Long userId) {
        return new SessionalPlanCreateCommand(
                userId,
                SESSION_NAME,
                SESSION_PERIOD
        );
    }

    public static SessionalPlanUpdateCommand createSessionalPlanUpdateCommand(
            String targetSessionName,
            SessionPeriod targetSessionPeriod
    ) {
        return createSessionalPlanUpdateCommand(SESSIONAL_PLAN_ID, targetSessionName, targetSessionPeriod);
    }

    public static SessionalPlanUpdateCommand createSessionalPlanUpdateCommand(
            Long sessionalPlanId,
            String targetSessionName,
            SessionPeriod targetSessionPeriod
    ) {
        return new SessionalPlanUpdateCommand(
                sessionalPlanId,
                USER_ID,
                targetSessionName,
                targetSessionPeriod
        );
    }

    public static SessionalPlanDeleteCommand createSessionalPlanDeleteCommand(Long sessionalPlanId) {
        return new SessionalPlanDeleteCommand(USER_ID, sessionalPlanId);
    }
}
