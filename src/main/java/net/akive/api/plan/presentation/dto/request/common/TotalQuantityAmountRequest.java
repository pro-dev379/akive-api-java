package net.akive.api.plan.presentation.dto.request.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static net.akive.api.plan.domain.PlanPolicy.QUANTITY_MEMO_MAX_LENGTH;
import static net.akive.api.plan.domain.PlanPolicy.QUANTITY_UNIT_MAX_LENGTH;

public record TotalQuantityAmountRequest(
        @NotNull @Min(0) Integer quantityAmount,
        @NotBlank @Size(max = QUANTITY_UNIT_MAX_LENGTH) String unit,
        @Size(max = QUANTITY_MEMO_MAX_LENGTH) String quantityMemo
) {
}
