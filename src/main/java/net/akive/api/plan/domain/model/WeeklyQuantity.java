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

    WeeklyQuantity increaseFirstDailyQuantity(int amount) {
        if (amount == 0) {
            return this;
        }

        List<DailyQuantity> adjustedDailyQuantities = new ArrayList<>(this.dailyQuantities);
        DailyQuantity firstDailyQuantity = adjustedDailyQuantities.getFirst().increaseQuantity(amount);
        adjustedDailyQuantities.set(0, firstDailyQuantity);

        return reconstructWithDailyQuantities(adjustedDailyQuantities);
    }

    QuantityReductionResult reduceQuantity(int amount) {
        if (amount == 0) {
            return new QuantityReductionResult(this, 0);
        }

        int remainingAmount = amount;
        List<DailyQuantity> adjustedDailyQuantities = new ArrayList<>(this.dailyQuantities);

        for (int i = 0; i < this.dailyQuantities.size(); i++) {
            DailyQuantity dailyQuantity = this.dailyQuantities.get(i);
            DailyQuantity adjustedDailyQuantity = dailyQuantity.reduceQuantity(remainingAmount);

            remainingAmount -= dailyQuantity.getDelayedQuantityAmount()
                    - adjustedDailyQuantity.getDelayedQuantityAmount();

            adjustedDailyQuantities.set(i, adjustedDailyQuantity);

            if (remainingAmount == 0) {
                break;
            }
        }

        return new QuantityReductionResult(
                reconstructWithDailyQuantities(adjustedDailyQuantities),
                remainingAmount
        );
    }

    int getCompletedQuantityAmount() {
        return dailyQuantities.stream()
                .mapToInt(DailyQuantity::getCompletedQuantityAmount)
                .sum();
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

    private WeeklyQuantity reconstructWithDailyQuantities(List<DailyQuantity> dailyQuantities) {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(
                sumDailyQuantityAmount(dailyQuantities),
                this.weeklyQuantityAmount.quantityMemo().value()
        );
        return WeeklyQuantity.builder()
                .weeklyQuantityId(this.weeklyQuantityId)
                .weekPeriod(this.weekPeriod)
                .weeklyQuantityAmount(weeklyQuantityAmount)
                .dailyQuantities(List.copyOf(dailyQuantities))
                .build();
    }

    private int sumDailyQuantityAmount(List<DailyQuantity> dailyQuantities) {
        return dailyQuantities.stream()
                .mapToInt(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount().value())
                .sum();
    }

    record QuantityReductionResult(WeeklyQuantity weeklyQuantity, int remainingAmount) {
    }
}
