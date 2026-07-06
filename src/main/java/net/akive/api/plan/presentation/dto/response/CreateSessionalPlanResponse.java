package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

public record CreateSessionalPlanResponse(Long sessionalPlanId) {

    public static CreateSessionalPlanResponse from(SessionalPlanSummaryResult result) {
        return new CreateSessionalPlanResponse(result.sessionalPlanId());
    }
}