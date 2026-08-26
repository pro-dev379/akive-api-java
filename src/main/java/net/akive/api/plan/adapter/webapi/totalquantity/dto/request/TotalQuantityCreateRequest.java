package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.akive.api.plan.adapter.webapi.common.dto.request.SubjectRequest;
import net.akive.api.plan.adapter.webapi.common.dto.request.TotalQuantityAmountRequest;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityCreateCommand;

public record TotalQuantityCreateRequest(
        @NotNull @Valid SubjectRequest subject,
        @NotNull @Valid TotalQuantityAmountRequest totalQuantityAmount
) {

    public TotalQuantityCreateCommand to(Long userId, Long sessionalPlanId) {
        return TotalQuantityCreateCommand.of(
                sessionalPlanId,
                userId,
                subject.name(),
                subject.category(),
                totalQuantityAmount().quantityAmount(),
                totalQuantityAmount().quantityUnit(),
                totalQuantityAmount.quantityMemo()
        );
    }
}
