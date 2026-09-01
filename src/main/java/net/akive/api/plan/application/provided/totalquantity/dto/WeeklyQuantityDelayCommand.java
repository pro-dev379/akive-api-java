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
        QuantityAmount weeklyAmountToDelay
) {

    public WeeklyQuantityDelayCommand {
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(targetWeeklyQuantityId);
        requireNonNull(weeklyAmountToDelay);
    }

    public static WeeklyQuantityDelayCommand of(
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            Long targetWeeklyQuantityId,
            int weeklyAmountToDelay
    ) {
        return new WeeklyQuantityDelayCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                targetWeeklyQuantityId,
                new QuantityAmount(weeklyAmountToDelay)
        );
    }
}
