package net.akive.api.plan.application.provided.totalquantity.dto;

import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record TotalQuantityUpdateCommand(
        Long totalQuantityId,
        Long sessionalPlanId,
        Long userId,
        Subject targetSubject,
        TotalQuantityAmount targetTotalQuantityAmount
) {

    public TotalQuantityUpdateCommand {
        requirePositive(totalQuantityId);
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        requireNonNull(targetSubject);
        requireNonNull(targetTotalQuantityAmount);
    }

    public static TotalQuantityUpdateCommand of(
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            String subjectName,
            String subjectCategory,
            int quantityAmount,
            String quantityUnit,
            String quantityMemo
    ) {

        return new TotalQuantityUpdateCommand(
                totalQuantityId,
                sessionalPlanId,
                userId,
                new Subject(subjectName, subjectCategory),
                TotalQuantityAmount.of(quantityAmount, quantityUnit, quantityMemo)
        );
    }
}
