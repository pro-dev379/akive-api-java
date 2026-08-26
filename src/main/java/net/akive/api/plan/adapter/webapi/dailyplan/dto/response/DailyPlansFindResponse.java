package net.akive.api.plan.adapter.webapi.dailyplan.dto.response;

import net.akive.api.plan.adapter.webapi.common.dto.response.SubjectResponse;
import net.akive.api.plan.application.result.DailyPlan;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityUnit;

import java.util.List;

public record DailyPlansFindResponse(List<DailyPlanResponse> dailyPlans) {

    public static DailyPlansFindResponse from(List<DailyPlan> todos) {
        return new DailyPlansFindResponse(
                todos.stream().map(DailyPlanResponse::from).toList()
        );
    }

    public record DailyPlanResponse(
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId,
            Long dailyQuantityId,
            SubjectResponse subject,
            AmountResponse amount
    ) {

        private static DailyPlanResponse from(DailyPlan result) {
            return new DailyPlanResponse(
                    result.sessionalPlanId(),
                    result.totalQuantityId(),
                    result.weeklyQuantityId(),
                    result.dailyQuantity().getDailyQuantityId(),
                    SubjectResponse.from(result.subject()),
                    AmountResponse.of(result.dailyQuantity().getDailyQuantityAmount(), result.quantityUnit())
            );
        }
    }

    public record AmountResponse(
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount,
            String quantityUnit,
            String quantityMemo
    ) {

        private static AmountResponse of(
                DailyQuantityAmount dailyQuantityAmount,
                QuantityUnit quantityUnit
        ) {
            return new AmountResponse(
                    dailyQuantityAmount.quantityAmount().value(),
                    dailyQuantityAmount.completedQuantityAmount().value(),
                    dailyQuantityAmount.delayedQuantityAmount().value(),
                    quantityUnit.value(),
                    dailyQuantityAmount.quantityMemo().value()
            );
        }
    }
}
