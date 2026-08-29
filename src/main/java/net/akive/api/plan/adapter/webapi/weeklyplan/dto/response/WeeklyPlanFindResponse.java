package net.akive.api.plan.adapter.webapi.weeklyplan.dto.response;

import net.akive.api.plan.adapter.webapi.common.dto.response.DailyQuantityAmountResponse;
import net.akive.api.plan.adapter.webapi.common.dto.response.SessionWeekResponse;
import net.akive.api.plan.adapter.webapi.common.dto.response.SubjectResponse;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.PlanDate;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record WeeklyPlanFindResponse(
        SessionWeekResponse sessionWeek,
        List<PlanDateResponse> planDates,
        List<WeeklyQuantityResponse> weeklyQuantities
) {

    public static WeeklyPlanFindResponse from(WeeklyPlan weeklyPlan) {
        return new WeeklyPlanFindResponse(
                SessionWeekResponse.from(weeklyPlan.sessionWeek()),
                weeklyPlan.planDates().stream().map(PlanDateResponse::from).toList(),
                weeklyPlan.weeklyQuantities().stream().map(WeeklyQuantityResponse::from).toList()
        );
    }

    public record PlanDateResponse(LocalDate date, DayOfWeek dayOfWeek) {
        private static PlanDateResponse from(PlanDate planDate) {
            return new PlanDateResponse(planDate.date(), planDate.dayOfWeek());
        }
    }

    public record WeeklyQuantityResponse(
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId,
            SubjectResponse subject,
            WeeklyQuantityAmountResponse weeklyQuantityAmount,
            List<DailyQuantityResponse> dailyQuantities
    ) {

        public static WeeklyQuantityResponse from(WeeklyPlan.WeeklyPlanItem result) {
            return new WeeklyQuantityResponse(
                    result.sessionalPlanId(),
                    result.totalQuantityId(),
                    result.weeklyQuantity().getWeeklyQuantityId(),
                    SubjectResponse.from(result.subject()),
                    WeeklyQuantityAmountResponse.of(
                            result.weeklyQuantity().getWeeklyQuantityAmount(),
                            result.quantityUnit()
                    ),
                    result.weeklyQuantity().getDailyQuantities().stream().map(DailyQuantityResponse::from).toList()
            );
        }
    }

    public record DailyQuantityResponse(
            Long dailyQuantityId,
            PlanDateResponse planDate,
            DailyQuantityAmountResponse dailyQuantityAmount
    ) {

        private static DailyQuantityResponse from(DailyQuantity result) {
            return new DailyQuantityResponse(
                    result.getDailyQuantityId(),
                    new PlanDateResponse(result.getDate(), result.getDate().getDayOfWeek()),
                    DailyQuantityAmountResponse.from(result.getDailyQuantityAmount())
            );
        }
    }

    public record WeeklyQuantityAmountResponse(
            int quantityAmount,
            String quantityUnit,
            String quantityMemo
    ) {
        private static WeeklyQuantityAmountResponse of(
                WeeklyQuantityAmount weeklyQuantityAmount,
                QuantityUnit quantityUnit
        ) {
            return new WeeklyQuantityAmountResponse(
                    weeklyQuantityAmount.quantityAmount().value(),
                    quantityUnit.value(),
                    weeklyQuantityAmount.quantityMemo().value()
            );
        }
    }
}
