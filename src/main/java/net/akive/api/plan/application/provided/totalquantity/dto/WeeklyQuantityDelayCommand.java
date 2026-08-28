package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyQuantityDelayCommand(
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        Long targetWeeklyQuantityId,
        QuantityAmount quantityAmountToDelay
) {

    public WeeklyQuantityDelayCommand {
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(targetWeeklyQuantityId);
        requireNonNull(quantityAmountToDelay);
    }

    public static WeeklyQuantityDelayCommand of(
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            Long targetTotalQuantityId,
            int quantityAmountToDelay
    ) {
        return new WeeklyQuantityDelayCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetTotalQuantityId,
                new QuantityAmount(quantityAmountToDelay)
        );
    }
}
