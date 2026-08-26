package net.akive.api.plan.adapter.persistence.weeklyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import net.akive.api.plan.application.required.WeeklyPlanRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.WeekPeriod;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeeklyPlanJpaAdapter implements WeeklyPlanRepository {

    private final WeeklyQuantityJpaRepository weeklyQuantityJpaRepository;

    @Override
    public List<WeeklyPlanData> findWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    ) {
        return weeklyQuantityJpaRepository
                .findWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                        sessionalPlanId,
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
