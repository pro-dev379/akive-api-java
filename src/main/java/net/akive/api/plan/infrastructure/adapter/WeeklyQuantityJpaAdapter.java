package net.akive.api.plan.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.LoadWeeklyQuantityPort;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanDailyQuantityData;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.PlanDate;
import net.akive.api.plan.domain.WeekPeriod;
import net.akive.api.plan.infrastructure.repository.WeeklyQuantityQueryRepository;
import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.projection.WeeklyPlanWeeklyQuantityProjection;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WeeklyQuantityJpaAdapter implements LoadWeeklyQuantityPort {

    private final WeeklyQuantityQueryRepository weeklyQuantityQueryRepository;

    @Override
    public List<WeeklyPlanWeeklyQuantityData> loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    ) {
        List<WeeklyPlanWeeklyQuantityProjection> projections =
                weeklyQuantityQueryRepository.findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                        sessionalPlanId,
                        WeekPeriodEntity.from(weekPeriod)
                );

        return projections.stream()
                .collect(Collectors.groupingBy(
                        WeeklyPlanWeeklyQuantityProjection::weeklyQuantityId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(this::toWeeklyPlanWeeklyQuantityDate)
                .toList();
    }

    private WeeklyPlanWeeklyQuantityData toWeeklyPlanWeeklyQuantityDate(List<WeeklyPlanWeeklyQuantityProjection> projections) {
        WeeklyPlanWeeklyQuantityProjection first = projections.getFirst();

        return new WeeklyPlanWeeklyQuantityData(
                first.sessionalPlanId(),
                first.totalQuantityId(),
                first.weeklyQuantityId(),
                first.subject().to(),
                first.weeklyQuantityAmount().to(),
                first.unit(),
                projections.stream()
                        .map(this::toWeeklyPlanDailyQuantityData)
                        .toList()
        );
    }

    private WeeklyPlanDailyQuantityData toWeeklyPlanDailyQuantityData(WeeklyPlanWeeklyQuantityProjection projection) {

        return new WeeklyPlanDailyQuantityData(
                projection.DailyQuantityId(),
                projection.dailyQuantityAmount().to(),
                PlanDate.from(projection.date())
        );
    }
}