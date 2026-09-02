package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DailyAmountDelayRequest(
        @NotNull @Min(0) Long receivingDailyQuantityId,
        @NotNull @Min(0) Integer dailyAmountToDelay
) {
}
