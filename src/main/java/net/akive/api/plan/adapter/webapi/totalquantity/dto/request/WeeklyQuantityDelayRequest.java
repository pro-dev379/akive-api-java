package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WeeklyQuantityDelayRequest(
        @NotNull @Min(0) Long targetWeeklyQuantityId,
        @NotNull @Min(0) Integer quantityAmountToDelay
) {
}
