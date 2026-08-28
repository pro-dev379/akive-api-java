package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.plan.domain.value.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_QUANTITY_DATE_OUT_OF_WEEK;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_AMOUNT_MISMATCH;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION;
import static net.akive.api.common.exception.business.ConflictError.WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;

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
        requireNotEmpty(dailyQuantities);
        validateDailyQuantitiesInWeekPeriod(dailyQuantities, weekPeriod);
        validateWeeklyQuantityAmountMatchesDailyQuantities(weeklyQuantityAmount, dailyQuantities);

        return WeeklyQuantity.builder()
                .weeklyQuantityId(requirePositive(weeklyQuantityId))
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(requireNonNull(weeklyQuantityAmount))
                .dailyQuantities(List.copyOf(dailyQuantities))
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

    void increase(int amount) {
        if (amount == 0) {
            return;
        }

        this.dailyQuantities.getFirst().increase(amount);

        updateWeeklyQuantityAmountByDailyQuantities();
    }

    QuantityReductionResult reduceDailyQuantities(int amount) {
        if (amount == 0) {
            return new QuantityReductionResult(0, 0);
        }

        int remainingAmount = amount;

        for (DailyQuantity dailyQuantity : this.dailyQuantities) {
            DailyQuantity.QuantityReductionResult result = dailyQuantity.reduce(remainingAmount);

            remainingAmount -= result.quantityAmountToReduce();

            if (remainingAmount == 0) {
                break;
            }
        }

        updateWeeklyQuantityAmountByDailyQuantities();

        return new QuantityReductionResult(remainingAmount, amount - remainingAmount);
    }

    QuantityReductionResult reduce(WeeklyQuantityAmount reducedWeeklyQuantityAmount) {
        requireNonNull(reducedWeeklyQuantityAmount);

        int quantityAmountToReduce = calculateReductionAmount(reducedWeeklyQuantityAmount);

        validateReducible(reducedWeeklyQuantityAmount, quantityAmountToReduce);

        if (quantityAmountToReduce == 0) {
            this.weeklyQuantityAmount = reducedWeeklyQuantityAmount;

            return new QuantityReductionResult(0, 0);
        }

        QuantityReductionResult result = reduceDailyQuantities(quantityAmountToReduce);
        this.weeklyQuantityAmount = reducedWeeklyQuantityAmount;

        return new QuantityReductionResult(result.remainingAmount(), quantityAmountToReduce);
    }

    int calculateReductionAmount(WeeklyQuantityAmount reducedWeeklyQuantityAmount) {
        requireNonNull(reducedWeeklyQuantityAmount);

        return this.weeklyQuantityAmount.quantityAmount().value()
                - reducedWeeklyQuantityAmount.quantityAmount().value();
    }

    int getCompletedQuantityAmount() {
        return dailyQuantities.stream()
                .mapToInt(DailyQuantity::getCompletedQuantityAmount)
                .sum();
    }

    private static void validateDailyQuantitiesInWeekPeriod(
            List<DailyQuantity> dailyQuantities,
            WeekPeriod weekPeriod
    ) {
        requireNonNull(weekPeriod);

        dailyQuantities.forEach(dailyQuantity -> {
            LocalDate date = dailyQuantity.getDate();
            if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
                throw new BadRequestException(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
            }
        });
    }

    private static void validateWeeklyQuantityAmountMatchesDailyQuantities(
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        requireNonNull(weeklyQuantityAmount);

        int dailyQuantityAmount = sumDailyQuantityAmount(dailyQuantities);

        if (weeklyQuantityAmount.quantityAmount().value() != dailyQuantityAmount) {
            throw new BadRequestException(WEEKLY_QUANTITY_AMOUNT_MISMATCH);
        }
    }

    private static int sumDailyQuantityAmount(List<DailyQuantity> dailyQuantities) {
        return dailyQuantities.stream()
                .mapToInt(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount().value())
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

    private void updateWeeklyQuantityAmountByDailyQuantities() {
        this.weeklyQuantityAmount = WeeklyQuantityAmount.of(
                sumDailyQuantityAmount(this.dailyQuantities),
                this.weeklyQuantityAmount.quantityMemo().value()
        );
    }

    private void validateReducible(WeeklyQuantityAmount reducedWeeklyQuantityAmount, int quantityAmountToReduce) {
        if (quantityAmountToReduce < 0) {
            throw new BadRequestException(WEEKLY_QUANTITY_INCREASE_NOT_ALLOWED);
        }

        if (reducedWeeklyQuantityAmount.quantityAmount().value() < getCompletedQuantityAmount()) {
            throw new ConflictException(WEEKLY_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);
        }
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

    record QuantityReductionResult(int remainingAmount, int quantityAmountToReduce) {
    }
}
