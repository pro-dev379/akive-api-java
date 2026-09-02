package net.akive.api.plan.adapter.webapi.totalquantity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.akive.api.plan.adapter.webapi.common.dto.request.SubjectRequest;
import net.akive.api.plan.adapter.webapi.common.dto.request.TotalQuantityAmountRequest;
import net.akive.api.plan.application.provided.totalquantity.dto.TotalQuantityUpdateCommand;

public record TotalQuantityUpdateRequest(
        @NotNull @Valid SubjectRequest targetSubject,
        @NotNull @Valid TotalQuantityAmountRequest targetTotalQuantityAmount
) {

    public TotalQuantityUpdateCommand to(Long userId, Long sessionalPlanId, Long totalQuantityId) {
        return TotalQuantityUpdateCommand.of(
                totalQuantityId,
                sessionalPlanId,
                userId,
                this.targetSubject.name(),
                this.targetSubject.category(),
                targetTotalQuantityAmount().quantityAmount(),
                targetTotalQuantityAmount().quantityUnit(),
                this.targetTotalQuantityAmount.quantityMemo()
        );
    }
}
