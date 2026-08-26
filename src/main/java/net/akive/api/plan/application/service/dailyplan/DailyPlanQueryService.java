package net.akive.api.plan.application.service.dailyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.provided.dailyplan.DailyPlanFinder;
import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.required.DailyPlanRepository;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.application.result.DailyPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DailyPlanQueryService implements DailyPlanFinder {

    private final DailyPlanRepository dailyPlanRepository;

    @Override
    public List<DailyPlan> find(DailyPlansFindQuery query) {
        return dailyPlanRepository.findDailyPlanDataByUserIdAndDate(query.userId(), query.date())
                .stream()
                .map(this::toDailyPlan)
                .toList();
    }

    private DailyPlan toDailyPlan(DailyPlanData dailyPlanData) {
        return new DailyPlan(
                dailyPlanData.sessionalPlanId(),
                dailyPlanData.totalQuantityId(),
                dailyPlanData.weeklyQuantityId(),
                dailyPlanData.subject(),
                dailyPlanData.dailyQuantity(),
                dailyPlanData.quantityUnit()
        );
    }
}
