package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

public record SessionalPlanItemResponse(
        Long sessionalPlanId,
        String sessionName,
        SessionPeriodResponse sessionPeriod
){

    public static SessionalPlanItemResponse from(SessionalPlanSummaryResult result) {
        return new SessionalPlanItemResponse(
                result.sessionalPlanId(),
                result.sessionName(),
                SessionPeriodResponse.of(
                        result.sessionPeriod().startOn(),
                        result.sessionPeriod().endOn()
                )
        );
    }
}