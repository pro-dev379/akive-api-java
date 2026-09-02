package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityMemo;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyAmountUpdateCommand(
        Long dailyQuantityId,
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        QuantityAmount targetDailyAmount,
        QuantityMemo targetDailyMemo
) {

    public DailyAmountUpdateCommand {
        requirePositive(sessionalPlanId);
        requirePositive(totalQuantityId);
        requirePositive(weeklyQuantityId);
        requirePositive(dailyQuantityId);
        requireNonNull(targetDailyAmount);
    }

    public static DailyAmountUpdateCommand of(
            Long dailyQuantityId,
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            int targetDailyAmount,
            String targetDailyMemo
    ) {
        return new DailyAmountUpdateCommand(
                dailyQuantityId,
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                new QuantityAmount(targetDailyAmount),
                new QuantityMemo(targetDailyMemo)
        );
    }
}
