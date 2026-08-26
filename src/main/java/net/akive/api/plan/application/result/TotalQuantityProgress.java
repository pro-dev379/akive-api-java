package net.akive.api.plan.application.result;

import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record TotalQuantityProgress(
        Long totalQuantityId,
        Subject subject,
        TotalQuantityAmount totalQuantityAmount,
        List<WeeklyQuantityProgress> weeklyQuantities
) {

    public TotalQuantityProgress {
        requirePositive(totalQuantityId);
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);
        weeklyQuantities = List.copyOf(requireNonNull(weeklyQuantities));
    }

    public static TotalQuantityProgress of(TotalQuantity totalQuantity, List<SessionWeek> sessionWeeks) {
        requireNonNull(totalQuantity);
        requireNonNull(sessionWeeks);

        Map<WeekPeriod, SessionWeek> sessionWeekByPeriod = sessionWeeks.stream()
                .collect(Collectors.toMap(
                        SessionWeek::weekPeriod,
                        Function.identity()
                ));

        return new TotalQuantityProgress(
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount(),
                totalQuantity.getWeeklyQuantities()
                        .stream()
                        .map(weeklyQuantity -> toWeeklyQuantityProgress(
                                totalQuantity.getTotalQuantityId(),
                                weeklyQuantity,
                                sessionWeekByPeriod
                        ))
                        .toList()
        );
    }

    private static WeeklyQuantityProgress toWeeklyQuantityProgress(
            Long totalQuantityId,
            WeeklyQuantity weeklyQuantity,
            Map<WeekPeriod, SessionWeek> sessionWeekByPeriod
    ) {
        WeekPeriod weekPeriod = weeklyQuantity.getWeekPeriod();
        SessionWeek sessionWeek = requireNonNull(sessionWeekByPeriod.get(weekPeriod));
        DailyQuantityAmount summedDailyQuantityAmount = sumDailyQuantityAmount(weeklyQuantity);
        WeeklyQuantityProgress.WeeklyQuantityProgressAmount weeklyQuantityAmount =
                new WeeklyQuantityProgress.WeeklyQuantityProgressAmount(
                        weeklyQuantity.getWeeklyQuantityAmount(),
                        summedDailyQuantityAmount.completedQuantityAmount(),
                        summedDailyQuantityAmount.delayedQuantityAmount()
                );

        return new WeeklyQuantityProgress(
                weeklyQuantity.getWeeklyQuantityId(),
                totalQuantityId,
                sessionWeek,
                weeklyQuantityAmount
        );
    }

    private static DailyQuantityAmount sumDailyQuantityAmount(WeeklyQuantity weeklyQuantity) {
        int completedQuantityAmount = weeklyQuantity.getDailyQuantities()
                .stream()
                .mapToInt(dailyQuantity -> dailyQuantity.getDailyQuantityAmount()
                        .completedQuantityAmount()
                        .value())
                .sum();
        int delayedQuantityAmount = weeklyQuantity.getDailyQuantities()
                .stream()
                .mapToInt(dailyQuantity -> dailyQuantity.getDailyQuantityAmount()
                        .delayedQuantityAmount()
                        .value())
                .sum();

        return DailyQuantityAmount.of(
                completedQuantityAmount + delayedQuantityAmount,
                completedQuantityAmount,
                delayedQuantityAmount,
                weeklyQuantity.getWeeklyQuantityAmount().quantityMemo().value()
        );
    }
}
