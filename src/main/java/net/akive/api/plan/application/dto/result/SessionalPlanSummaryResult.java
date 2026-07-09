package net.akive.api.plan.application.dto.result;

import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;

public record SessionalPlanSummaryResult(
        Long sessionalPlanId,
        String sessionName,
        SessionPeriod sessionPeriod
) {

    public static SessionalPlanSummaryResult from(SessionalPlan sessionalPlan) {
        return new SessionalPlanSummaryResult(
                sessionalPlan.getSessionalPlanId(),
                sessionalPlan.getSessionName(),
                sessionalPlan.getSessionPeriod()
        );
    }
}