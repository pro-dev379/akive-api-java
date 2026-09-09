package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.PlanDate;
import net.akive.api.plan.domain.value.WeekPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class DailyQuantityFixture {

    public static final Long FIRST_DAILY_QUANTITY_ID = 1L;
    public static final Long SECOND_DAILY_QUANTITY_ID = 2L;
    public static final Long UNKNOWN_DAILY_QUANTITY_ID = Long.MAX_VALUE;
    public static final LocalDate FIRST_DATE = LocalDate.of(2026, 1, 1);
    public static final LocalDate SECOND_DATE = LocalDate.of(2026, 1, 2);
    public static final String TARGET_DAILY_MEMO = "첫날 조정";
    public static final int TARGET_DAILY_AMOUNT = 50;
    public static final int TARGET_DAILY_COMPLETED_AMOUNT = 50;
    public static final int DAILY_AMOUNT_TO_DELAY = 10;

    private DailyQuantityFixture() {
    }

    public static DailyQuantity createReconstructedDailyQuantity(Long dailyQuantityId) {
        return DailyQuantity.reconstruct(
                dailyQuantityId,
                FIRST_DATE,
                DailyQuantityAmount.of(100, 0, 100)
        );
    }

    public static DailyQuantity createReconstructedDailyQuantity(Long dailyQuantityId, WeekPeriod weekPeriod, int amount) {
        int completedAmount = amount == 0 ? 0 : 30;

        return createReconstructedDailyQuantity(dailyQuantityId, weekPeriod, amount, completedAmount);
    }

    public static DailyQuantity createReconstructedDailyQuantity(
            Long dailyQuantityId,
            WeekPeriod weekPeriod,
            int amount,
            int completedAmount
    ) {

        return DailyQuantity.reconstruct(
                dailyQuantityId,
                weekPeriod.startOn(),
                DailyQuantityAmount.of(amount, completedAmount, amount - completedAmount)
        );
    }

    public static List<DailyQuantity> createDailyQuantitiesForWeek(WeekPeriod weekPeriod, int firstDayAmount) {
        List<PlanDate> planDates = weekPeriod.splitIntoDates();

        return IntStream.range(0, planDates.size())
                .mapToObj(index -> {
                    int dailyAmount = index == 0 ? firstDayAmount : 0;

                    return DailyQuantity.reconstruct(
                            (long) index + 1,
                            planDates.get(index).date(),
                            DailyQuantityAmount.of(dailyAmount, 0, dailyAmount)
                    );
                })
                .toList();
    }

    public static List<DailyQuantity> createDailyQuantitiesFrom(
            LocalDate firstDate,
            DailyQuantityAmount... dailyQuantityAmounts
    ) {
        return IntStream.range(0, dailyQuantityAmounts.length)
                .mapToObj(index -> DailyQuantity.reconstruct(
                        (long) index + 1,
                        firstDate.plusDays(index),
                        dailyQuantityAmounts[index]
                ))
                .toList();
    }
}
