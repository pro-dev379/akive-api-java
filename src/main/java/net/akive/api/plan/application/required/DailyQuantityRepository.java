package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.value.QuantityCompletionAmount;

import java.time.LocalDate;
import java.util.List;

public interface DailyQuantityRepository {

    List<DailyPlanData> findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(Long userId, LocalDate date);

    List<QuantityCompletionAmount> findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    );
}
