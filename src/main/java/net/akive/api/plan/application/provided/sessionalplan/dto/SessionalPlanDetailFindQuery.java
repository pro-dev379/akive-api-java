package net.akive.api.plan.application.provided.sessionalplan.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record SessionalPlanDetailFindQuery(Long userId, Long sessionalPlanId) {

    public SessionalPlanDetailFindQuery {
        requirePositive(userId);
        requirePositive(sessionalPlanId);
    }
}
