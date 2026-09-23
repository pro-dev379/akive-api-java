package net.akive.api.plan.adapter.webapi.sessionalplan.dto.response;

import net.akive.api.plan.domain.value.CompletionRate;

import java.math.BigDecimal;

public record CompletionRateFindResponse(BigDecimal completionRate) {

    public static CompletionRateFindResponse from(CompletionRate completionRate) {
        return new CompletionRateFindResponse(completionRate.value());
    }
}
