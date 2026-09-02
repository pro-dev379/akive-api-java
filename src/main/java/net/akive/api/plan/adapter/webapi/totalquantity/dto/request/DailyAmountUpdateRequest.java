package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static net.akive.api.plan.domain.policy.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;

public record DailyAmountUpdateRequest(@NotNull @Valid DailyAmountRequest targetDailyAmount) {

    public record DailyAmountRequest(
            @NotNull @Min(0) Integer quantityAmount,
            @Size(max = QUANTITY_MEMO_MAX_LENGTH) String quantityMemo
    ) {
    }
}
