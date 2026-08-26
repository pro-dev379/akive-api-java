package net.akive.api.plan.adapter.webapi.sessionalplan.dto.response;

import net.akive.api.plan.adapter.webapi.common.dto.response.SessionPeriodResponse;
import net.akive.api.plan.domain.model.SessionalPlan;

import java.util.List;

public record SessionalPlansFindResponse(List<SessionalPlanResponse> sessionalPlans) {

    public static SessionalPlansFindResponse from(List<SessionalPlan> sessionalPlans) {
        return new SessionalPlansFindResponse(
                sessionalPlans.stream().map(SessionalPlanResponse::from).toList()
        );
    }

    public record SessionalPlanResponse(
            Long sessionalPlanId,
            String sessionName,
            SessionPeriodResponse sessionPeriod
    ) {
        private static SessionalPlanResponse from(SessionalPlan sessionalPlan) {
            return new SessionalPlanResponse(
                    sessionalPlan.getSessionalPlanId(),
                    sessionalPlan.getSessionName(),
                    SessionPeriodResponse.from(sessionalPlan.getSessionPeriod())
            );
        }
    }
}
