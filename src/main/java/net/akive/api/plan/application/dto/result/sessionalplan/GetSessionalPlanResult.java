package net.akive.api.plan.application.dto.result.sessionalplan;

import net.akive.api.plan.application.dto.result.common.*;
import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityAmountData;
import net.akive.api.plan.application.port.out.dto.SessionalPlanWeeklyQuantityData;

import java.util.List;

public record GetSessionalPlanResult(
        Long sessionalPlanId,
        String sessionName,
        SessionPeriodResult sessionPeriod,
        List<SessionWeekResult> weeks,
        List<TotalQuantityResult> totalQuantities
) {

    public record TotalQuantityResult(
            Long totalQuantityId,
            SubjectResult subject,
            TotalQuantityAmountResult totalQuantityAmount,
            List<WeeklyQuantityResult> weeklyQuantities
    ) {

        public static TotalQuantityResult from(SessionalPlanTotalQuantityData data) {
            return new TotalQuantityResult(
                    data.totalQuantityId(),
                    SubjectResult.from(data.subject()),
                    new TotalQuantityAmountResult(
                            data.totalQuantityAmount().quantityAmount().value(),
                            data.totalQuantityAmount().unit(),
                            data.totalQuantityAmount().quantityMemo().value()
                    ),
                    data.weeklyQuantities().stream().map(WeeklyQuantityResult::from).toList()
            );
        }
    }

    public record WeeklyQuantityResult(
            Long weeklyQuantityId,
            SessionWeekResult sessionWeek,
            WeeklyQuantityAmountResult weeklyQuantityAmount
    ) {

        public static WeeklyQuantityResult from(SessionalPlanWeeklyQuantityData data) {
            return new WeeklyQuantityResult(
                    data.weeklyQuantityId(),
                    SessionWeekResult.from(data.sessionWeek()),
                    WeeklyQuantityAmountResult.from(data.weeklyQuantityAmount())
            );
        }
    }

    public record WeeklyQuantityAmountResult(
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount,
            String quantityMemo
    ) {

        public static WeeklyQuantityAmountResult from(SessionalPlanWeeklyQuantityAmountData data) {
            return new WeeklyQuantityAmountResult(
                    data.quantityAmount(),
                    data.completedQuantityAmount(),
                    data.delayedQuantityAmount(),
                    data.quantityMemo()
            );
        }
    }
}