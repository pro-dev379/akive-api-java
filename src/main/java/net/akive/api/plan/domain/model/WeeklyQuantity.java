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
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT;

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
        validateReconstructable(weekPeriod, weeklyQuantityAmount, dailyQuantities);

        return WeeklyQuantity.builder()
                .weeklyQuantityId(requirePositive(weeklyQuantityId))
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(requireNonNull(weeklyQuantityAmount))
                .dailyQuantities(requireNotEmpty(List.copyOf(dailyQuantities)))
                .build();
    }

    static WeeklyQuantity create(
            TotalQuantityAmount totalQuantityAmount,
            SessionPeriod sessionPeriod,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            boolean isFirstWeek
    ) {
        validateCreatable(totalQuantityAmount, sessionPeriod, weekPeriod, weeklyQuantityAmount);

        List<DailyQuantity> dailyQuantities = createDailyQuantities(
                weekPeriod,
                weeklyQuantityAmount,
                isFirstWeek
        );

        return WeeklyQuantity.builder()
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(requireNonNull(weeklyQuantityAmount))
                .dailyQuantities(requireNotEmpty(List.copyOf(dailyQuantities)))
                .build();
    }

    void increase(QuantityAmount weeklyAmountToIncrease) {
        if (weeklyAmountToIncrease.isZero()) {
            return;
        }

        this.dailyQuantities.getFirst().increase(weeklyAmountToIncrease);

        updateWeeklyQuantityAmountByDailyQuantities();
    }

    void reduce(WeeklyQuantityAmount targetWeeklyQuantityAmount) {
        requireNonNull(targetWeeklyQuantityAmount);
        validateReducible(targetWeeklyQuantityAmount.quantityAmount());

        QuantityAmountDelta weeklyAmountDelta = calculateAmountDeltaTo(targetWeeklyQuantityAmount.quantityAmount());

        if (weeklyAmountDelta.isZero()) {
            this.weeklyQuantityAmount = targetWeeklyQuantityAmount;
            return;
        }

        reduceDailyQuantities(weeklyAmountDelta.absoluteAmount());

        this.weeklyQuantityAmount = targetWeeklyQuantityAmount;
    }

    QuantityAmount reduceDailyQuantities(QuantityAmount weeklyAmountToReduce) {
        QuantityAmount remainingAmountToReduce = weeklyAmountToReduce;

        if (remainingAmountToReduce.isZero()) {
            return QuantityAmount.ZERO;
        }

        for (DailyQuantity dailyQuantity : this.dailyQuantities) {
            QuantityAmount appliedReductionAmount = dailyQuantity.reduceAsMuchAsPossible(remainingAmountToReduce);

            remainingAmountToReduce = remainingAmountToReduce.minus(appliedReductionAmount);

            if (remainingAmountToReduce.isZero()) {
                break;
            }
        }

        updateWeeklyQuantityAmountByDailyQuantities();

        return remainingAmountToReduce;
    }

    void delayTo(WeeklyQuantity targetWeeklyQuantity, QuantityAmount weeklyAmountToDelay) {
        requireNonNull(targetWeeklyQuantity);
        requireNonNull(weeklyAmountToDelay);

        validateDelayable(weeklyAmountToDelay);

        reduceDailyQuantities(weeklyAmountToDelay);
        targetWeeklyQuantity.increase(weeklyAmountToDelay);
    }

    QuantityAmountDelta calculateAmountDeltaTo(QuantityAmount targetWeeklyAmount) {
        requireNonNull(targetWeeklyAmount);

        return this.weeklyQuantityAmount.quantityAmount().deltaTo(targetWeeklyAmount);
    }

    QuantityAmount getCompletedAmount() {
        return dailyQuantities.stream()
                .map(DailyQuantity::getCompletedAmount)
                .reduce(QuantityAmount.ZERO, QuantityAmount::plus);
    }

    private static void validateReconstructable(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        requireNotEmpty(dailyQuantities);

        validateDailyQuantitiesInWeekPeriod(dailyQuantities, weekPeriod);
        validateWeeklyQuantityAmountMatchesDailyAmounts(weeklyQuantityAmount, dailyQuantities);
    }

    private static void validateCreatable(
            TotalQuantityAmount totalQuantityAmount,
            SessionPeriod sessionPeriod,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount
    ) {
        requireNonNull(totalQuantityAmount);
        requireNonNull(sessionPeriod);
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);

        if (weekPeriod.startOn().isBefore(sessionPeriod.startOn()) ||
                weekPeriod.endOn().isAfter(sessionPeriod.endOn())) {
            throw new BadRequestException(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION);
        }

        if (totalQuantityAmount.quantityAmount().isLessThan(weeklyQuantityAmount.quantityAmount())) {
            throw new BadRequestException(WEEKLY_AMOUNT_EXCEEDS_TOTAL_AMOUNT);
        }
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

    private static void validateWeeklyQuantityAmountMatchesDailyAmounts(
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        requireNonNull(weeklyQuantityAmount);

        QuantityAmount summedDailyAmount = sumDailyQuantityAmount(dailyQuantities);

        if (!weeklyQuantityAmount.quantityAmount().equals(summedDailyAmount)) {
            throw new BadRequestException(WEEKLY_AMOUNT_MISMATCH);
        }
    }

    private static QuantityAmount sumDailyQuantityAmount(List<DailyQuantity> dailyQuantities) {
        return dailyQuantities.stream()
                .map(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::plus);
    }

    private static List<DailyQuantity> createDailyQuantities(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            boolean isFirstWeek
    ) {
        List<PlanDate> planDates = weekPeriod.splitIntoDates();
        List<DailyQuantity> dailyQuantities = new ArrayList<>();

        for (int i = 0; i < planDates.size(); i++) {
            QuantityAmount dailyAmount = isFirstWeek && i == 0
                    ? weeklyQuantityAmount.quantityAmount()
                    : QuantityAmount.ZERO;

            DailyQuantity dailyQuantity = DailyQuantity.create(
                    weekPeriod,
                    weeklyQuantityAmount,
                    planDates.get(i).date(),
                    new DailyQuantityAmount(dailyAmount, QuantityAmount.ZERO, dailyAmount, QuantityMemo.NONE)
            );

            dailyQuantities.add(dailyQuantity);
        }

        return dailyQuantities;
    }

    private void updateWeeklyQuantityAmountByDailyQuantities() {
        this.weeklyQuantityAmount = new WeeklyQuantityAmount(
                sumDailyQuantityAmount(this.dailyQuantities),
                this.weeklyQuantityAmount.quantityMemo()
        );
    }

    private void validateReducible(QuantityAmount targetWeeklyAmount) {
        QuantityAmountDelta weeklyAmountDelta = calculateAmountDeltaTo(targetWeeklyAmount);

        if (weeklyAmountDelta.isPositive()) {
            throw new BadRequestException(WEEKLY_AMOUNT_INCREASE_NOT_ALLOWED);
        }

        if (targetWeeklyAmount.isLessThan(getCompletedAmount())) {
            throw new ConflictException(WEEKLY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }
    }

    private void validateDelayable(QuantityAmount weeklyAmountToDelay) {
        requireNonNull(weeklyAmountToDelay);

        QuantityAmount delayedWeeklyAmount = getDelayedQuantityAmount();

        if (delayedWeeklyAmount.isLessThan(weeklyAmountToDelay)) {
            throw new BadRequestException(WEEKLY_DELAY_AMOUNT_EXCEEDS_CURRENT_WEEKLY_DELAYED_AMOUNT);
        }
    }

    private QuantityAmount getDelayedQuantityAmount() {
        return dailyQuantities.stream()
                .map(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::plus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeeklyQuantity that = (WeeklyQuantity) o;

        return this.weeklyQuantityId != null && Objects.equals(this.weeklyQuantityId, that.weeklyQuantityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
