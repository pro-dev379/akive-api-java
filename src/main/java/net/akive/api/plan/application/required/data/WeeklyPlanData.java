package net.akive.api.plan.application.required.data;

import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;

public record WeeklyPlanData(
        Long sessionalPlanId,
        Long totalQuantityId,
        Subject subject,
        QuantityUnit quantityUnit,
        WeeklyQuantity weeklyQuantity
) {
}
