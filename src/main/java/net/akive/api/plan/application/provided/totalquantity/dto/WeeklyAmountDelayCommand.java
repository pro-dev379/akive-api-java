package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyAmountDelayCommand(
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        Long receivingWeeklyQuantityId,
        QuantityAmount weeklyAmountToDelay
) {

    public WeeklyAmountDelayCommand {
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(receivingWeeklyQuantityId);
        requireNonNull(weeklyAmountToDelay);
    }

    public static WeeklyAmountDelayCommand of(
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            Long receivingWeeklyQuantityId,
            int weeklyAmountToDelay
    ) {
        return new WeeklyAmountDelayCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingWeeklyQuantityId,
                new QuantityAmount(weeklyAmountToDelay)
        );
    }
}
