package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyAmountDelayCommand(
        Long dailyQuantityId,
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        Long receivingDailyQuantityId,
        QuantityAmount dailyAmountToDelay
) {

    public DailyAmountDelayCommand {
        requirePositive(dailyQuantityId);
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requirePositive(receivingDailyQuantityId);
        requireNonNull(dailyAmountToDelay);
    }

    public static DailyAmountDelayCommand of(
            Long dailyQuantityId,
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            Long receivingDailyQuantityId,
            int dailyAmountToDelay
    ) {
        return new DailyAmountDelayCommand(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                receivingDailyQuantityId,
                new QuantityAmount(dailyAmountToDelay)
        );
    }
}
