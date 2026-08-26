package net.akive.api.plan.application.provided.weeklyplan.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyPlanFindQuery(
        Long sessionalPlanId,
        Long userId,
        Integer weekNum
) {

    public WeeklyPlanFindQuery {
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(weekNum);
    }
}
