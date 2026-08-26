package net.akive.api.plan.application.required.data;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;

public record DailyPlanData(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Subject subject,
        DailyQuantity dailyQuantity,
        QuantityUnit quantityUnit
) {
}
