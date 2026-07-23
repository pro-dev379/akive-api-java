package net.akive.api.plan.presentation.dto.request.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TotalQuantityAmountRequest(
        @NotNull @Min(0) Integer quantityAmount,
        @NotBlank @Size(max = 10) String unit,
        @Size(max = 100) String quantityMemo
) {
}