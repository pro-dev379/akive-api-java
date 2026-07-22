package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.exception.PlanException;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT;
import static net.akive.api.plan.exception.PlanErrorCode.DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class DailyQuantity {

    private Long dailyQuantityId;
    private WeeklyQuantity weeklyQuantity;
    private LocalDate date;
    private DailyQuantityAmount dailyQuantityAmount;

    public static DailyQuantity createForWeeklyQuantity(
            WeeklyQuantity weeklyQuantity,
            LocalDate date,
            DailyQuantityAmount dailyQuantityAmount
    ) {
        requireNonNull(weeklyQuantity);
        requireNonNull(date);
        validateDateInWeekPeriod(date, weeklyQuantity.getWeekPeriod());
        requireNonNull(dailyQuantityAmount);

        validateDailyQuantityAmountInWeeklyQuantityAmount(
                dailyQuantityAmount,
                weeklyQuantity.getWeeklyQuantityAmount()
        );

        return DailyQuantity.builder()
                .weeklyQuantity(weeklyQuantity)
                .date(date)
                .dailyQuantityAmount(dailyQuantityAmount)
                .build();
    }

    public static DailyQuantity create(
            Long dailyQuantityId,
            WeeklyQuantity weeklyQuantity,
            LocalDate date,
            DailyQuantityAmount dailyQuantityAmount
    ) {
        requireNonNull(weeklyQuantity);
        requireNonNull(date);
        validateDateInWeekPeriod(date, weeklyQuantity.getWeekPeriod());
        requireNonNull(dailyQuantityAmount);

        validateDailyQuantityAmountInWeeklyQuantityAmount(
                dailyQuantityAmount,
                weeklyQuantity.getWeeklyQuantityAmount()
        );

        return DailyQuantity.builder()
                .dailyQuantityId(requirePositive(dailyQuantityId))
                .weeklyQuantity(weeklyQuantity)
                .date(date)
                .dailyQuantityAmount(dailyQuantityAmount)
                .build();
    }

    private static void validateDateInWeekPeriod(LocalDate date, WeekPeriod weekPeriod) {
        if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
            throw new PlanException(DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD);
        }
    }

    private static void validateDailyQuantityAmountInWeeklyQuantityAmount(
            DailyQuantityAmount dailyQuantityAmount,
            WeeklyQuantityAmount weeklyQuantityAmount
    ) {
        if (weeklyQuantityAmount.quantityAmount().value() < dailyQuantityAmount.quantityAmount().value()) {
            throw new PlanException(DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
        }
    }
}
