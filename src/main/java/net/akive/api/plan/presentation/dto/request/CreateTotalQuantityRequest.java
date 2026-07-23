package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.presentation.dto.request.common.SubjectRequest;
import net.akive.api.plan.presentation.dto.request.common.TotalQuantityAmountRequest;

public record CreateTotalQuantityRequest(
        @NotNull @Valid SubjectRequest subject,
        @NotNull @Valid TotalQuantityAmountRequest totalQuantityAmount
) {

    public CreateTotalQuantityCommand to(Long userId, Long sessionalPlanId) {
        return CreateTotalQuantityCommand.of(
                sessionalPlanId,
                userId,
                subject.name(),
                subject.category(),
                totalQuantityAmount().quantityAmount(),
                totalQuantityAmount().unit(),
                totalQuantityAmount.quantityMemo()
        );
    }
}