package net.akive.api.plan.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.LoadWeeklyQuantitiesPort;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanDailyQuantityData;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.PlanDate;
import net.akive.api.plan.domain.WeekPeriod;
import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.projection.WeeklyPlanWeeklyQuantitiesProjection;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WeeklyQuantityJpaAdapter implements LoadWeeklyQuantitiesPort {

    private final WeeklyQuantityQueryRepository weeklyQuantityQueryRepository;

    @Override
    public List<WeeklyPlanWeeklyQuantityData> loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    ) {
        List<WeeklyPlanWeeklyQuantitiesProjection> projections =
                weeklyQuantityQueryRepository.findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                        sessionalPlanId,
                        WeekPeriodEntity.from(weekPeriod)
                );

        return projections.stream()
                .collect(Collectors.groupingBy(
                        WeeklyPlanWeeklyQuantitiesProjection::weeklyQuantityId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(this::toWeeklyPlanWeeklyQuantityDate)
                .toList();
    }

    private WeeklyPlanWeeklyQuantityData toWeeklyPlanWeeklyQuantityDate(List<WeeklyPlanWeeklyQuantitiesProjection> projections) {
        WeeklyPlanWeeklyQuantitiesProjection first = projections.getFirst();

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

    private WeeklyPlanDailyQuantityData toWeeklyPlanDailyQuantityData(WeeklyPlanWeeklyQuantitiesProjection projection) {

        return new WeeklyPlanDailyQuantityData(
                projection.DailyQuantityId(),
                projection.dailyQuantityAmount().to(),
                PlanDate.from(projection.date())
        );
    }
}