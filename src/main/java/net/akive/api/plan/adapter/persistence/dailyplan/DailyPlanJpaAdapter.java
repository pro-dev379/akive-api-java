package net.akive.api.plan.adapter.persistence.dailyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.adapter.persistence.entity.DailyQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import net.akive.api.plan.application.required.DailyPlanRepository;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.value.QuantityUnit;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyPlanJpaAdapter implements DailyPlanRepository {

    private final DailyQuantityJpaRepository dailyQuantityJpaRepository;

    @Override
    public List<DailyPlanData> findDailyPlanDataByUserIdAndDate(Long userId, LocalDate date) {
        return dailyQuantityJpaRepository
                .findDailyQuantitiesByUserIdAndDate(userId, date)
                .stream()
                .map(this::toDailyPlanData)
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
