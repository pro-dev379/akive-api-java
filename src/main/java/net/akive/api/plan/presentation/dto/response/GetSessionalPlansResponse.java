package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

import java.util.List;

public record GetSessionalPlansResponse(List<SessionalPlanItemResponse> sessionalPlans) {

    public static GetSessionalPlansResponse from(List<SessionalPlanSummaryResult> results) {
        return new GetSessionalPlansResponse(
                results.stream().map(SessionalPlanItemResponse::from).toList()
        );
    }
}