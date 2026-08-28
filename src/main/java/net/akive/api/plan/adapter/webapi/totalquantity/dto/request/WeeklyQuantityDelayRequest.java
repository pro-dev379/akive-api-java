package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.constraints.NotNull;

public record WeeklyQuantityDelayRequest(
        @NotNull Long targetWeeklyQuantityId,
        @NotNull Integer quantityAmountToDelay
) {
}
