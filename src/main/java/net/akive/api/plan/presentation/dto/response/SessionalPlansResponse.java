package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

import java.util.List;

public record SessionalPlansResponse(List<SessionalPlanItemResponse> sessionalPlans) {

    public static SessionalPlansResponse from(List<SessionalPlanSummaryResult> results) {
        return new SessionalPlansResponse(
                results.stream().map(SessionalPlanItemResponse::from).toList()
        );
    }
}