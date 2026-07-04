package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanResult;

import java.util.List;

public record SessionalPlansResponse(List<SessionalPlanItemResponse> sessionalPlans) {

    public static SessionalPlansResponse from(List<SessionalPlanResult> results) {
        return new SessionalPlansResponse(
                results.stream()
                        .map(SessionalPlanItemResponse::from)
                        .toList()
        );
    }
}