package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.WeeklyQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.application.result.WeeklyPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_PLAN_ITEM_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WeeklyQuantityQueryService implements WeeklyQuantityFinder {

    private final WeeklyQuantityRepository weeklyQuantityRepository;

    @Override
    public WeeklyPlan.WeeklyPlanItem findWeeklyPlanItem(WeeklyPlanItemFindQuery query) throws NotFoundException {
        return weeklyQuantityRepository
                .findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        query.sessionalPlanId(),
                        query.userId(),
                        query.totalQuantityId(),
                        query.weeklyQuantityId()
                )
                .map(this::toWeeklyPlanItem)
                .orElseThrow(() -> new NotFoundException(WEEKLY_PLAN_ITEM_NOT_FOUND));
    }

    private WeeklyPlan.WeeklyPlanItem toWeeklyPlanItem(WeeklyPlanData weeklyPlanData) {
        return new WeeklyPlan.WeeklyPlanItem(
                weeklyPlanData.sessionalPlanId(),
                weeklyPlanData.totalQuantityId(),
                weeklyPlanData.subject(),
                weeklyPlanData.quantityUnit(),
                weeklyPlanData.weeklyQuantity()
        );
    }
}
