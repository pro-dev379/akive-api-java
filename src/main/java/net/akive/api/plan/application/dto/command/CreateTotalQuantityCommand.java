package net.akive.api.plan.application.dto.command;

import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;

public record CreateTotalQuantityCommand(
        Long sessionalPlanId,
        Long userId,
        Subject subject,
        TotalQuantityAmount totalQuantityAmount
) {
    public static CreateTotalQuantityCommand of(
            Long sessionalPlanId,
            Long userId,
            String subjectName,
            String subjectCategory,
            int quantityAmount,
            String unit,
            String quantityMemo
    ) {
        return new CreateTotalQuantityCommand(
                sessionalPlanId,
                userId,
                new Subject(subjectName, subjectCategory),
                TotalQuantityAmount.of(quantityAmount, unit, quantityMemo)
        );
    }
}
