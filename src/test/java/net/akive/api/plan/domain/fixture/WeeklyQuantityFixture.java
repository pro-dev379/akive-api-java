package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.*;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.TOTAL_AMOUNT;

public class WeeklyQuantityFixture {

    public static final Long FIRST_WEEKLY_QUANTITY_ID = 1L;
    public static final Long SECOND_WEEKLY_QUANTITY_ID = 2L;
    public static final Long THIRD_WEEKLY_QUANTITY_ID = 3L;
    public static final Long UNKNOWN_WEEKLY_QUANTITY_ID = Long.MAX_VALUE;
    public static final WeekPeriod FIRST_WEEK_PERIOD = new WeekPeriod(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 4)
    );
    public static final WeekPeriod SECOND_WEEK_PERIOD = new WeekPeriod(
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 11)
    );
    public static final WeekPeriod THIRD_WEEK_PERIOD = new WeekPeriod(
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 1, 12)
    );
    public static final WeeklyQuantityAmount TARGET_WEEKLY_QUANTITY_AMOUNT =
            WeeklyQuantityAmount.of(20, "첫 주 조정");
    public static final WeeklyQuantityAmount TARGET_REDUCED_WEEKLY_QUANTITY_AMOUNT =
            WeeklyQuantityAmount.of(20);
    public static final String TARGET_WEEKLY_MEMO = "업데이트된 메모";
    public static final int WEEKLY_AMOUNT_TO_DELAY = 10;

    private WeeklyQuantityFixture() {
    }

    public static WeeklyQuantity createWeeklyQuantity(
            Long weeklyQuantityId,
            LocalDate startOn,
            LocalDate endOn,
            int amount
    ) {
        WeekPeriod weekPeriod = new WeekPeriod(startOn, endOn);

        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                weekPeriod,
                WeeklyQuantityAmount.of(amount),
                createDailyQuantitiesForWeek(weekPeriod, amount)
        );
    }

    public static WeeklyQuantity createReconstructedWeeklyQuantity(Long weeklyQuantityId) {
        return createWeeklyQuantity(
                weeklyQuantityId,
                FIRST_WEEK_PERIOD.startOn(),
                FIRST_WEEK_PERIOD.endOn(),
                DailyQuantityAmount.of(100, 0, 100)
        );
    }

    public static WeeklyQuantity createWeeklyQuantity(DailyQuantityAmount... dailyQuantityAmounts) {
        return createWeeklyQuantity(
                FIRST_WEEKLY_QUANTITY_ID,
                FIRST_WEEK_PERIOD.startOn(),
                FIRST_WEEK_PERIOD.endOn(),
                dailyQuantityAmounts
        );
    }

    public static WeeklyQuantity createWeeklyQuantity(
            Long weeklyQuantityId,
            LocalDate startOn,
            LocalDate endOn,
            DailyQuantityAmount... dailyQuantityAmounts
    ) {
        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                new WeekPeriod(startOn, endOn),
                WeeklyQuantityAmount.of(sumDailyAmounts(dailyQuantityAmounts)),
                createDailyQuantitiesFrom(startOn, dailyQuantityAmounts)
        );
    }

    public static WeeklyQuantity createWeeklyQuantityWithCompletedAmount(
            Long weeklyQuantityId,
            LocalDate startOn,
            LocalDate endOn,
            int quantityAmount,
            int completedQuantityAmount
    ) {
        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                new WeekPeriod(startOn, endOn),
                WeeklyQuantityAmount.of(quantityAmount),
                List.of(createReconstructedDailyQuantity(
                        weeklyQuantityId,
                        new WeekPeriod(startOn, endOn),
                        quantityAmount,
                        completedQuantityAmount
                ))
        );
    }

    static WeeklyQuantity createReconstructedWeeklyQuantity(SessionWeek sessionWeek) {
        Long weeklyQuantityId = (long) sessionWeek.weekNum();
        WeekPeriod weekPeriod = sessionWeek.weekPeriod();
        int amount = sessionWeek.weekNum() == 1 ? TOTAL_AMOUNT : 0;

        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                weekPeriod,
                WeeklyQuantityAmount.of(amount),
                List.of(createReconstructedDailyQuantity(weeklyQuantityId, weekPeriod, amount))
        );
    }

    private static int sumDailyAmounts(DailyQuantityAmount... dailyQuantityAmounts) {
        int sum = 0;

        for (DailyQuantityAmount dailyQuantityAmount : dailyQuantityAmounts) {
            sum += dailyQuantityAmount.quantityAmount().value();
        }

        return sum;
    }
}
