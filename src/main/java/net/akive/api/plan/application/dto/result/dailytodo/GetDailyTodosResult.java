package net.akive.api.plan.application.dto.result.dailytodo;

import net.akive.api.plan.application.dto.result.common.SubjectResult;
import net.akive.api.plan.application.port.out.dto.DailyTodoData;
import net.akive.api.plan.domain.DailyQuantityAmount;

import java.util.List;

public record GetDailyTodosResult(List<DailyTodoResult> todos) {

    public static GetDailyTodosResult from(List<DailyTodoData> data) {
        return new GetDailyTodosResult(data.stream().map(DailyTodoResult::from).toList());
    }

    public record DailyTodoResult(
            Long sessionalPlanId,
            Long totalQuantityId,
            Long weeklyQuantityId,
            Long dailyQuantityId,
            SubjectResult subject,
            DailyTodoAmountResult dailyTodoQuantityAmount
    ) {

        public static DailyTodoResult from(DailyTodoData data) {
            return new DailyTodoResult(
                    data.sessionalPlanId(),
                    data.totalQuantityId(),
                    data.weeklyQuantityId(),
                    data.dailyQuantityId(),
                    SubjectResult.from(data.subject()),
                    DailyTodoAmountResult.of(data.dailyQuantityAmount(), data.unit())
            );
        }

        public record DailyTodoAmountResult(
                int quantityAmount,
                int completedQuantityAmount,
                int delayedQuantityAmount,
                String unit,
                String quantityMemo
        ) {

            public static DailyTodoAmountResult of(DailyQuantityAmount dailyQuantityAmount, String unit) {
                return new DailyTodoAmountResult(
                        dailyQuantityAmount.quantityAmount().value(),
                        dailyQuantityAmount.completedQuantityAmount().value(),
                        dailyQuantityAmount.delayedQuantityAmount().value(),
                        unit,
                        dailyQuantityAmount.quantityMemo().value()
                );
            }
        }
    }
}
