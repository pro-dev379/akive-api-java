package net.akive.api.plan.adapter.persistence.dailyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.adapter.persistence.entity.DailyQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import net.akive.api.plan.application.required.DailyQuantityRepository;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.value.QuantityCompletionAmount;
import net.akive.api.plan.domain.value.QuantityUnit;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyQuantityJpaAdapter implements DailyQuantityRepository {

    private final DailyQuantityJpaRepository dailyQuantityJpaRepository;

    @Override
    public List<DailyPlanData> findDailyPlansByUserIdAndDateOrderByPlanHierarchyAsc(
            Long userId,
            LocalDate date
    ) {
        return dailyQuantityJpaRepository
                .findAllByUserIdAndDateOrderByPlanHierarchyAsc(userId, date)
                .stream()
                .map(this::toDailyPlanData)
                .toList();
    }

    @Override
    public List<QuantityCompletionAmount> findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    ) {
        return dailyQuantityJpaRepository
                .findTotalQuantityCompletionAmountsByUserIdAndDateBetween(userId, startOn, endOn)
                .stream()
                .map(projection -> new QuantityCompletionAmount(
                        Math.toIntExact(projection.getQuantityAmount()),
                        Math.toIntExact(projection.getCompletedQuantityAmount())
                ))
                .toList();
    }

    private DailyPlanData toDailyPlanData(DailyQuantityEntity dailyQuantityEntity) {
        WeeklyQuantityEntity weeklyQuantityEntity = dailyQuantityEntity.getWeeklyQuantity();
        TotalQuantityEntity totalQuantityEntity = weeklyQuantityEntity.getTotalQuantity();

        return new DailyPlanData(
                totalQuantityEntity.getSessionalPlan().getSessionalPlanId(),
                totalQuantityEntity.getTotalQuantityId(),
                weeklyQuantityEntity.getWeeklyQuantityId(),
                totalQuantityEntity.getSubject().to(),
                dailyQuantityEntity.to(),
                new QuantityUnit(totalQuantityEntity.getTotalQuantityAmount().getQuantityUnit())
        );
    }
}
