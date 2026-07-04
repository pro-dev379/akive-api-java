package net.akive.api.plan.application.dto.result;

import net.akive.api.plan.domain.SessionalPlan;

import java.time.LocalDate;

public record SessionalPlanResult(
        Long sessionalPlanId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
) {

    public static SessionalPlanResult from(SessionalPlan sessionalPlan) {
        return new SessionalPlanResult(
                sessionalPlan.getSessionalPlanId(),
                sessionalPlan.getSessionName(),
                sessionalPlan.getSessionStartOn(),
                sessionalPlan.getSessionEndOn()
        );
    }
}