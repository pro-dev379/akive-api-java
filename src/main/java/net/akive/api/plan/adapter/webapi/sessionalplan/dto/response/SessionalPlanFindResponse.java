package net.akive.api.plan.adapter.webapi.sessionalplan.dto.response;

import net.akive.api.plan.adapter.webapi.common.dto.response.SessionPeriodResponse;
import net.akive.api.plan.adapter.webapi.common.dto.response.SessionWeekResponse;
import net.akive.api.plan.adapter.webapi.common.dto.response.SubjectResponse;
import net.akive.api.plan.adapter.webapi.common.dto.response.TotalQuantityAmountResponse;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.application.result.WeeklyQuantityProgress;

import java.util.List;

public record SessionalPlanFindResponse(
        Long sessionalPlanId,
        String sessionName,
        SessionPeriodResponse sessionPeriod,
        List<SessionWeekResponse> sessionWeeks,
        List<TotalQuantityResponse> totalQuantities
) {

    public static SessionalPlanFindResponse from(SessionalPlanDetail detail) {
        return new SessionalPlanFindResponse(
                detail.sessionalPlan().getSessionalPlanId(),
                detail.sessionalPlan().getSessionName(),
                SessionPeriodResponse.from(detail.sessionalPlan().getSessionPeriod()),
                detail.sessionalPlan().getSessionPeriod().splitIntoWeeks().stream()
                        .map(SessionWeekResponse::from)
                        .toList(),
                detail.totalQuantities().stream().map(TotalQuantityResponse::from).toList()
        );
    }

    public record TotalQuantityResponse(
            Long totalQuantityId,
            SubjectResponse subject,
            TotalQuantityAmountResponse totalQuantityAmount,
            List<WeeklyQuantityResponse> weeklyQuantities
    ) {

        public static TotalQuantityResponse from(TotalQuantityProgress result) {
            return new TotalQuantityResponse(
                    result.totalQuantityId(),
                    SubjectResponse.from(result.subject()),
                    TotalQuantityAmountResponse.from(result.totalQuantityAmount()),
                    result.weeklyQuantities().stream().map(WeeklyQuantityResponse::from).toList()
            );
        }
    }

    public record WeeklyQuantityResponse(
            Long weeklyQuantityId,
            SessionWeekResponse sessionWeek,
            WeeklyQuantityAmountResponse weeklyQuantityAmount
    ) {

        private static WeeklyQuantityResponse from(WeeklyQuantityProgress result) {
            return new WeeklyQuantityResponse(
                    result.weeklyQuantityId(),
                    SessionWeekResponse.from(result.sessionWeek()),
                    WeeklyQuantityAmountResponse.from(result.weeklyQuantityAmount())
            );
        }
    }

    public record WeeklyQuantityAmountResponse(
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount,
            String quantityMemo
    ) {

        private static WeeklyQuantityAmountResponse from(
                WeeklyQuantityProgress.WeeklyQuantityProgressAmount view
        ) {
            return new WeeklyQuantityAmountResponse(
                    view.weeklyQuantityAmount().quantityAmount().value(),
                    view.completedQuantityAmount().value(),
                    view.delayedQuantityAmount().value(),
                    view.weeklyQuantityAmount().quantityMemo().value()
            );
        }
    }
}
