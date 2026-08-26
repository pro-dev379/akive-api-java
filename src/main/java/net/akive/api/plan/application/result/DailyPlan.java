package net.akive.api.plan.application.result;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyPlan(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Subject subject,
        DailyQuantity dailyQuantity,
        QuantityUnit quantityUnit
) {

    public DailyPlan {
        requirePositive(sessionalPlanId);
        requirePositive(totalQuantityId);
        requirePositive(weeklyQuantityId);
        requireNonNull(subject);
        requireNonNull(dailyQuantity);
        requireNonNull(quantityUnit);
    }
}
