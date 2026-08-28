package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyQuantityReduceCommand;

import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public record WeeklyQuantityReduceRequest(
        @NotNull @Valid WeeklyQuantityAmountRequest weeklyQuantityAmount
) {

    public WeeklyQuantityReduceCommand to(
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId
    ) {
        return WeeklyQuantityReduceCommand.of(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                weeklyQuantityAmount.quantityAmount(),
                weeklyQuantityAmount.quantityMemo()
        );
    }

    record WeeklyQuantityAmountRequest(
            @NotNull @Min(0) Integer quantityAmount,
            @Size(max = QUANTITY_MEMO_MAX_LENGTH) String quantityMemo
    ) {
    }
}
