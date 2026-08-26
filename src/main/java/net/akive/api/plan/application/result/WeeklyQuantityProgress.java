package net.akive.api.plan.application.result;

import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyQuantityProgress(
        Long weeklyQuantityId,
        Long totalQuantityId,
        SessionWeek sessionWeek,
        WeeklyQuantityProgressAmount weeklyQuantityAmount
) {

    public WeeklyQuantityProgress {
        requirePositive(weeklyQuantityId);
        requirePositive(totalQuantityId);
        requireNonNull(sessionWeek);
        requireNonNull(weeklyQuantityAmount);
    }

    public record WeeklyQuantityProgressAmount(
            WeeklyQuantityAmount weeklyQuantityAmount,
            QuantityAmount completedQuantityAmount,
            QuantityAmount delayedQuantityAmount
    ) {

        public WeeklyQuantityProgressAmount {
            requireNonNull(weeklyQuantityAmount);
            requireNonNull(completedQuantityAmount);
            requireNonNull(delayedQuantityAmount);
        }
    }
}
