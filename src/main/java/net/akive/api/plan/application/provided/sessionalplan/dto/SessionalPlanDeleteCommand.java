package net.akive.api.plan.application.provided.sessionalplan.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record SessionalPlanDeleteCommand(Long userId, Long sessionalPlanId) {

    public SessionalPlanDeleteCommand {
        requirePositive(userId);
        requirePositive(sessionalPlanId);
    }
}
