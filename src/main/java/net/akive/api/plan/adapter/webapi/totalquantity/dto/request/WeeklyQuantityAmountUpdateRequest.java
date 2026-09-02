package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public record WeeklyQuantityAmountUpdateRequest(
        @NotNull @Valid WeeklyQuantityAmountRequest targetWeeklyQuantityAmount
) {

    public record WeeklyQuantityAmountRequest(
            @NotNull @Min(0) Integer quantityAmount,
            @Size(max = QUANTITY_MEMO_MAX_LENGTH) String quantityMemo
    ) {
    }
}
