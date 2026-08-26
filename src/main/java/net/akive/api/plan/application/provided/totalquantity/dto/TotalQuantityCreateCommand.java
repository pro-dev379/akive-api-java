package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record TotalQuantityCreateCommand(
        Long sessionalPlanId,
        Long userId,
        Subject subject,
        TotalQuantityAmount totalQuantityAmount
) {

    public TotalQuantityCreateCommand {
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);
    }

    public static TotalQuantityCreateCommand of(
            Long sessionalPlanId,
            Long userId,
            String subjectName,
            String subjectCategory,
            int quantityAmount,
            String quantityUnit,
            String quantityMemo
    ) {

        return new TotalQuantityCreateCommand(
                sessionalPlanId,
                userId,
                new Subject(subjectName, subjectCategory),
                TotalQuantityAmount.of(quantityAmount, quantityUnit, quantityMemo)
        );
    }
}
