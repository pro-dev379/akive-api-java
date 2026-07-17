package net.akive.api.plan.application.dto.result.sessionalplan;

import net.akive.api.plan.application.dto.result.common.SessionPeriodResult;
import net.akive.api.plan.domain.SessionalPlan;

import java.util.List;

public record GetSessionalPlansResult(List<SessionalPlanResult> sessionalPlans) {

    public static GetSessionalPlansResult from(List<SessionalPlan> sessionalPlans) {
        return new GetSessionalPlansResult(sessionalPlans.stream().map(SessionalPlanResult::from).toList());
    }

    public record SessionalPlanResult(
            Long sessionalPlanId,
            String sessionName,
            SessionPeriodResult sessionPeriod
    ) {
        public static SessionalPlanResult from(SessionalPlan sessionalPlan) {
            return new SessionalPlanResult(
                    sessionalPlan.getSessionalPlanId(),
                    sessionalPlan.getSessionName(),
                    SessionPeriodResult.from(sessionalPlan.getSessionPeriod())
            );
        }
    }
}