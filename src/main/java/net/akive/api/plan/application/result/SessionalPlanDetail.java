package net.akive.api.plan.application.result;

import net.akive.api.plan.domain.model.SessionalPlan;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record SessionalPlanDetail(
        SessionalPlan sessionalPlan,
        List<TotalQuantityProgress> totalQuantities
) {

    public SessionalPlanDetail {
        requireNonNull(sessionalPlan);
        totalQuantities = List.copyOf(requireNonNull(totalQuantities));
    }
}
