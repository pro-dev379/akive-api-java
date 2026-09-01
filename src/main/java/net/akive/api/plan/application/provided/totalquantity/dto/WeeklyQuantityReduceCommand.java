package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.QuantityMemo;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyQuantityReduceCommand(
        Long weeklyQuantityId,
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        WeeklyQuantityAmount targetWeeklyQuantityAmount
) {

    public WeeklyQuantityReduceCommand {
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requireNonNull(targetWeeklyQuantityAmount);
    }

    public static WeeklyQuantityReduceCommand of(
            Long weeklyQuantityId,
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            int quantityAmount,
            String quantityMemo
    ) {
        return new WeeklyQuantityReduceCommand(
                weeklyQuantityId,
                totalQuantityId,
                sessionalPlanId,
                userId,
                new WeeklyQuantityAmount(
                        new QuantityAmount(quantityAmount),
                        new QuantityMemo(quantityMemo)
                )
        );
    }
}
