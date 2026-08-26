package net.akive.api.plan.application.provided.sessionalplan.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record SessionalPlanFindQuery(Long userId, Long sessionalPlanId) {

    public SessionalPlanFindQuery {
        requirePositive(userId);
        requirePositive(sessionalPlanId);
    }
}
