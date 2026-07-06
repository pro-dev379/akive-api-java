package net.akive.api.plan.application.dto.result;

import net.akive.api.plan.domain.SessionalPlan;

import java.time.LocalDate;

public record SessionalPlanSummaryResult(
        Long sessionalPlanId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
) {

    public static SessionalPlanSummaryResult from(SessionalPlan sessionalPlan) {
        return new SessionalPlanSummaryResult(
                sessionalPlan.getSessionalPlanId(),
                sessionalPlan.getSessionName(),
                sessionalPlan.getSessionStartOn(),
                sessionalPlan.getSessionEndOn()
        );
    }
}