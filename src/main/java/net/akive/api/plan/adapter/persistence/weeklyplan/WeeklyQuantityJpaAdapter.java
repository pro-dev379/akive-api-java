package net.akive.api.plan.adapter.persistence.weeklyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.WeekPeriod;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeeklyQuantityJpaAdapter implements WeeklyQuantityRepository {

    private final WeeklyQuantityJpaRepository weeklyQuantityJpaRepository;

    @Override
    public Optional<WeeklyPlanData> findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
            Long sessionalPlanId,
            Long userId,
            Long totalQuantityId,
            Long weeklyQuantityId
    ) {
        return weeklyQuantityJpaRepository
                .findBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
                        sessionalPlanId,
                        userId,
                        totalQuantityId,
                        weeklyQuantityId
                ).map(this::toWeeklyPlanData);
    }

    @Override
    public List<WeeklyPlanData> findAllWeeklyPlanDataBySessionalPlanIdAndUserIdAndWeekPeriodOrderByPlanHierarchyAsc(
            Long sessionalPlanId,
            Long userId,
            WeekPeriod weekPeriod
    ) {
        return weeklyQuantityJpaRepository
                .findWeeklyQuantitiesBySessionalPlanIdAndUserIdAndWeekPeriodOrderByPlanHierarchyAsc(
                        sessionalPlanId,
                        userId,
                        WeekPeriodEntity.from(weekPeriod)
                )
                .stream()
                .map(this::toWeeklyPlanData)
                .toList();
    }

    private WeeklyPlanData toWeeklyPlanData(WeeklyQuantityEntity weeklyQuantityEntity) {
        TotalQuantityEntity totalQuantityEntity = weeklyQuantityEntity.getTotalQuantity();

        return new WeeklyPlanData(
                totalQuantityEntity.getSessionalPlan().getSessionalPlanId(),
                totalQuantityEntity.getTotalQuantityId(),
                totalQuantityEntity.getSubject().to(),
                new QuantityUnit(totalQuantityEntity.getTotalQuantityAmount().getQuantityUnit()),
                weeklyQuantityEntity.to()
        );
    }
}
