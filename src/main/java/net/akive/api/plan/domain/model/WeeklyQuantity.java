package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.plan.domain.value.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantity {

    private Long weeklyQuantityId;

    private WeekPeriod weekPeriod;

    private WeeklyQuantityAmount weeklyQuantityAmount;

    private List<DailyQuantity> dailyQuantities;

    public static WeeklyQuantity reconstruct(
            Long weeklyQuantityId,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        return WeeklyQuantity.builder()
                .weeklyQuantityId(requirePositive(weeklyQuantityId))
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(requireNonNull(weeklyQuantityAmount))
                .dailyQuantities(requireNotEmpty(dailyQuantities))
                .build();
    }

    static WeeklyQuantity create(
            TotalQuantityAmount totalQuantityAmount,
            SessionPeriod sessionPeriod,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            boolean isInitialAllocationWeek
    ) {
        requireNonNull(totalQuantityAmount);
        requireNonNull(sessionPeriod);
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);
        validateWeekPeriodInSessionPeriod(weekPeriod, sessionPeriod);
        validateWeeklyQuantityAmountInTotalQuantityAmount(
                weeklyQuantityAmount,
                totalQuantityAmount
        );

        List<DailyQuantity> dailyQuantities = createDailyQuantities(
                weekPeriod,
                weeklyQuantityAmount,
                isInitialAllocationWeek
        );

        return WeeklyQuantity.builder()
                .weekPeriod(weekPeriod)
                .weeklyQuantityAmount(weeklyQuantityAmount)
                .dailyQuantities(List.copyOf(dailyQuantities))
                .build();
    }

    private static List<DailyQuantity> createDailyQuantities(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            boolean isInitialAllocationWeek
    ) {
        List<PlanDate> planDates = weekPeriod.splitIntoDates();
        List<DailyQuantity> dailyQuantities = new ArrayList<>();

        for (int i = 0; i < planDates.size(); i++) {
            int amount = isInitialAllocationWeek && i == 0
                    ? weeklyQuantityAmount.quantityAmount().value()
                    : 0;

            DailyQuantity dailyQuantity = DailyQuantity.create(
                    weekPeriod,
                    weeklyQuantityAmount,
                    planDates.get(i).date(),
                    DailyQuantityAmount.of(amount, 0, amount, null)
            );

            dailyQuantities.add(dailyQuantity);
        }

        return dailyQuantities;
    }

    private static void validateWeekPeriodInSessionPeriod(
            WeekPeriod weekPeriod,
            SessionPeriod sessionPeriod
    ) {
        if (weekPeriod.startOn().isBefore(sessionPeriod.startOn()) ||
                weekPeriod.endOn().isAfter(sessionPeriod.endOn())) {
            throw new BadRequestException(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION);
        }
    }

    private static void validateWeeklyQuantityAmountInTotalQuantityAmount(
            WeeklyQuantityAmount weeklyQuantityAmount,
            TotalQuantityAmount totalQuantityAmount
    ) {
        if (totalQuantityAmount.quantityAmount().value() < weeklyQuantityAmount.quantityAmount().value()) {
            throw new BadRequestException(WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY);
        }
    }
}
