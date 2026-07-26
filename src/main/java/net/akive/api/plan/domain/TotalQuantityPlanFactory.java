package net.akive.api.plan.domain;

import java.util.ArrayList;
import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public class TotalQuantityPlanFactory {

    public TotalQuantityPlan create(
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        TotalQuantity totalQuantity = TotalQuantity.createForUser(
                sessionalPlan,
                subject,
                totalQuantityAmount
        );

        List<SessionWeek> sessionWeeks = requireNonNull(sessionalPlan).getSessionPeriod().splitIntoWeeks();
        List<WeeklyQuantityPlan> weeklyQuantities = new ArrayList<>();

        for (int i = 0; i < sessionWeeks.size(); i++) {
            boolean firstWeek = i == 0;
            int weeklyAmount = firstWeek ? totalQuantityAmount.quantityAmount().value() : 0;
            WeeklyQuantity weeklyQuantity = WeeklyQuantity.createForTotalQuantity(
                    totalQuantity,
                    sessionalPlan.getSessionPeriod(),
                    sessionWeeks.get(i).weekPeriod(),
                    WeeklyQuantityAmount.of(weeklyAmount, null)
            );

            List<DailyQuantity> dailyQuantities = createDailyQuantities(weeklyQuantity, firstWeek);
            WeeklyQuantityPlan weeklyQuantityPlan = new WeeklyQuantityPlan(weeklyQuantity, dailyQuantities);
            weeklyQuantities.add(weeklyQuantityPlan);
        }

        return new TotalQuantityPlan(totalQuantity, weeklyQuantities);
    }

    private List<DailyQuantity> createDailyQuantities(
            WeeklyQuantity weeklyQuantity,
            boolean putAllAmountOnFirstDay
    ) {
        List<PlanDate> planDates = weeklyQuantity.getWeekPeriod().splitIntoDates();
        List<DailyQuantity> dailyQuantities = new ArrayList<>();

        for (int i = 0; i < planDates.size(); i++) {
            int amount = putAllAmountOnFirstDay && i == 0
                    ? weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value()
                    : 0;
            dailyQuantities.add(
                    DailyQuantity.createForWeeklyQuantity(
                            weeklyQuantity,
                            planDates.get(i).date(),
                            DailyQuantityAmount.of(amount, 0, amount, null)
                    )
            );
        }

        return dailyQuantities;
    }
}
