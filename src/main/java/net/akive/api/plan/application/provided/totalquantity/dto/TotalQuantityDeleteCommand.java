package net.akive.api.plan.application.provided.totalquantity.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record TotalQuantityDeleteCommand(Long sessionalPlanId, Long userId, Long totalQuantityId) {

    public TotalQuantityDeleteCommand {
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(totalQuantityId);
    }
}
