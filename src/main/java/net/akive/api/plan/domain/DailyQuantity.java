package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.exception.DailyQuantityException;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.exception.DailyQuantityErrorCode.AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT;
import static net.akive.api.plan.domain.exception.DailyQuantityErrorCode.DATE_OUT_OF_WEEK_PERIOD;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class DailyQuantity {

    private Long dailyQuantityId;
    private WeeklyQuantity weeklyQuantity;
    private LocalDate date;
    private int dailyQuantityAmount;
    private int completedQuantityAmount;
    private int delayQuantityAmount;
    private String dailyQuantityMemo;

    public static DailyQuantity create(
            Long dailyQuantityId,
            WeeklyQuantity weeklyQuantity,
            LocalDate date,
            int dailyQuantityAmount,
            int completedQuantityAmount,
            int delayQuantityAmount,
            String dailyQuantityMemo
    ) {
        requireNonNull(weeklyQuantity);
        requireNonNull(date);
        validateDateInWeekPeriod(date, weeklyQuantity.getWeekPeriod());
        validateWeeklyQuantityAmountInTotalQuantityAmount(
                dailyQuantityAmount,
                weeklyQuantity.getWeeklyQuantityAmount()
        );

        return DailyQuantity.builder()
                .dailyQuantityId(dailyQuantityId)
                .weeklyQuantity(weeklyQuantity)
                .date(date)
                .dailyQuantityAmount(requireNonNegative(dailyQuantityAmount))
                .completedQuantityAmount(requireNonNegative(completedQuantityAmount))
                .delayQuantityAmount(requireNonNegative(delayQuantityAmount))
                .dailyQuantityMemo(requireMaxLengthIfPresent(dailyQuantityMemo, 25))
                .build();
    }

    private static void validateDateInWeekPeriod(LocalDate date, WeekPeriod weekPeriod) {
        if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
            throw new DailyQuantityException(DATE_OUT_OF_WEEK_PERIOD);
        }
    }

    private static void validateWeeklyQuantityAmountInTotalQuantityAmount(
            int dailyQuantityAmount,
            int weeklyQuantityAmount
    ) {
        if (weeklyQuantityAmount < dailyQuantityAmount) {
            throw new DailyQuantityException(AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
        }
    }
}
