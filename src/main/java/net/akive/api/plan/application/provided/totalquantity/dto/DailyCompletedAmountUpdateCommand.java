package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyCompletedAmountUpdateCommand(
        Long dailyQuantityId,
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        QuantityAmount targetDailyCompletedAmount
) {

    public DailyCompletedAmountUpdateCommand {
        requirePositive(dailyQuantityId);
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requireNonNull(targetDailyCompletedAmount);
    }

    public static DailyCompletedAmountUpdateCommand of(
            Long dailyQuantityId,
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            int targetDailyCompletedAmount
    ) {
        return new DailyCompletedAmountUpdateCommand(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                new QuantityAmount(targetDailyCompletedAmount)
        );
    }
}
