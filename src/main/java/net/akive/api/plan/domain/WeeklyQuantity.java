package net.akive.api.plan.domain;

import lombok.*;
import net.akive.api.plan.domain.exception.WeeklyQuantityException;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.exception.WeeklyQuantityErrorCode.AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT;
import static net.akive.api.plan.domain.exception.WeeklyQuantityErrorCode.PERIOD_OUT_OF_SESSION_PERIOD;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantity {

    private Long weeklyQuantityId;
    private TotalQuantity totalQuantity;
    private WeekPeriod weekPeriod;
    private int weeklyQuantityAmount;
    private String weeklyQuantityMemo;
    private List<DailyQuantity> dailyQuantities;

    public static WeeklyQuantity create(
            Long weeklyQuantityId,
            TotalQuantity totalQuantity,
            WeekPeriod weekPeriod,
            int weeklyQuantityAmount,
            String weeklyQuantityMemo
    ) {
        requireNonNull(totalQuantity);
        requireNonNull(weekPeriod);

        validateWeekPeriodInSessionPeriod(
                weekPeriod,
                totalQuantity.getSessionalPlan().getSessionPeriod()
        );

        validateWeeklyQuantityAmountInTotalQuantityAmount(
                weeklyQuantityAmount,
                totalQuantity.getTotalQuantityAmount()
        );

        return WeeklyQuantity.builder()
                .weeklyQuantityId(requirePositive(weeklyQuantityId))
                .totalQuantity(requireNonNull(totalQuantity))
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(requireNonNegative(weeklyQuantityAmount))
                .weeklyQuantityMemo(requireMaxLengthIfPresent(weeklyQuantityMemo, 25))
                .build();
    }

    private static void validateWeekPeriodInSessionPeriod(
            WeekPeriod weekPeriod,
            SessionPeriod sessionPeriod
    ) {
        if (weekPeriod.startOn().isBefore(sessionPeriod.startOn()) ||
                weekPeriod.endOn().isAfter(sessionPeriod.endOn())) {
            throw new WeeklyQuantityException(PERIOD_OUT_OF_SESSION_PERIOD);
        }
    }

    private static void validateWeeklyQuantityAmountInTotalQuantityAmount(
            int weeklyQuantityAmount,
            int totalQuantityAmount
    ) {
        if (totalQuantityAmount < weeklyQuantityAmount) {
            throw new WeeklyQuantityException(AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT);
        }
    }
}
