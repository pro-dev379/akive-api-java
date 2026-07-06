package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

public record SessionalPlanCreateResponse(Long sessionalPlanId) {

    public static SessionalPlanCreateResponse from(SessionalPlanSummaryResult result) {
        return new SessionalPlanCreateResponse(result.sessionalPlanId());
    }
}