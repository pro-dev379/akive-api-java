package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

import java.time.LocalDate;

public record SessionalPlanItemResponse(
        Long sessionalPlanId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
){

    public static SessionalPlanItemResponse from(SessionalPlanSummaryResult result) {
        return new SessionalPlanItemResponse(
                result.sessionalPlanId(),
                result.sessionName(),
                result.sessionStartOn(),
                result.sessionEndOn()
        );
    }
}