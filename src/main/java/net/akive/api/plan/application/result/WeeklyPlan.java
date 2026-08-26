package net.akive.api.plan.application.result;

import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.PlanDate;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.plan.domain.value.Subject;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyPlan(
        SessionWeek sessionWeek,
        List<WeeklyPlanItem> weeklyQuantities
) {

    public WeeklyPlan {
        requireNonNull(sessionWeek);
        weeklyQuantities = List.copyOf(requireNonNull(weeklyQuantities));
    }

    public List<PlanDate> planDates() {
        return sessionWeek.weekPeriod().splitIntoDates();
    }

    public record WeeklyPlanItem(
            Long sessionalPlanId,
            Long totalQuantityId,
            Subject subject,
            QuantityUnit quantityUnit,
            WeeklyQuantity weeklyQuantity
    ) {

        public WeeklyPlanItem {
            requirePositive(sessionalPlanId);
            requirePositive(totalQuantityId);
            requireNonNull(subject);
            requireNonNull(quantityUnit);
            requireNonNull(weeklyQuantity);
        }
    }
}
