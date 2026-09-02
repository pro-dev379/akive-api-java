package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
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
import static net.akive.api.common.exception.business.NotFoundError.DAILY_QUANTITY_NOT_FOUND;

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

    void increaseBy(QuantityAmount weeklyAmountToIncrease) {
        if (weeklyAmountToIncrease.isZero()) {
            return;
        }

        this.weeklyQuantityAmount = this.weeklyQuantityAmount.increaseBy(weeklyAmountToIncrease);
        this.dailyQuantities.getFirst().increaseBy(weeklyAmountToIncrease);

        validateWeeklyAmountMatchesDailyAmounts();
    }

    void updateQuantityAmount(WeeklyQuantityAmount targetWeeklyQuantityAmount) {
        requireNonNull(targetWeeklyQuantityAmount);
        validateReducible(targetWeeklyQuantityAmount.quantityAmount());

        QuantityAmount weeklyAmountToReduce =
                calculateAmountToReduceTo(targetWeeklyQuantityAmount.quantityAmount());

        if (weeklyAmountToReduce.isZero()) {
            this.weeklyQuantityAmount = targetWeeklyQuantityAmount;
            return;
        }

        reduceDailyAmountsBy(weeklyAmountToReduce);

        this.weeklyQuantityAmount = targetWeeklyQuantityAmount;

        validateWeeklyAmountMatchesDailyAmounts();
    }

    void updateDailyAmount(
            Long dailyQuantityId,
            QuantityAmount targetDailyAmount,
            QuantityMemo targetDailyMemo
    ) {
        int dailyQuantityIndex = findDailyQuantityIndex(dailyQuantityId);
        DailyQuantity dailyQuantity = this.dailyQuantities.get(dailyQuantityIndex);
        QuantityAmount dailyAmountToReduce = dailyQuantity.calculateAmountToReduceTo(targetDailyAmount);

        validateNotLastDayReduction(dailyAmountToReduce, dailyQuantityIndex);

        dailyQuantity.updateAmount(targetDailyAmount, targetDailyMemo);

        carryOverReductionAmountToNextDay(dailyQuantityIndex, dailyAmountToReduce);

        validateWeeklyAmountMatchesDailyAmounts();
    }

    QuantityAmount reduceDailyAmountsBy(QuantityAmount weeklyAmountToReduce) {
        QuantityAmount remainingAmountToReduce = weeklyAmountToReduce;

        if (remainingAmountToReduce.isZero()) {
            return QuantityAmount.ZERO;
        }

        for (DailyQuantity dailyQuantity : this.dailyQuantities) {
            QuantityAmount appliedReductionAmount = dailyQuantity.reduceByAsMuchAsPossible(remainingAmountToReduce);

            remainingAmountToReduce = remainingAmountToReduce.subtract(appliedReductionAmount);

            if (remainingAmountToReduce.isZero()) {
                break;
            }
        }

        QuantityAmount totalAppliedReductionAmount = weeklyAmountToReduce.subtract(remainingAmountToReduce);
        this.weeklyQuantityAmount = this.weeklyQuantityAmount.reduceBy(totalAppliedReductionAmount);

        validateWeeklyAmountMatchesDailyAmounts();

        return remainingAmountToReduce;
    }

    void delayAmountTo(WeeklyQuantity targetWeeklyQuantity, QuantityAmount weeklyAmountToDelay) {
        validateDelayable(weeklyAmountToDelay);

        reduceDailyAmountsBy(weeklyAmountToDelay);
        targetWeeklyQuantity.increaseBy(weeklyAmountToDelay);
    }

    QuantityAmount calculateAmountToReduceTo(QuantityAmount targetWeeklyAmount) {
        validateReducible(targetWeeklyAmount);

        return this.weeklyQuantityAmount.quantityAmount().subtract(targetWeeklyAmount);
    }

    QuantityAmount getCompletedAmount() {
        return dailyQuantities.stream()
                .map(DailyQuantity::getCompletedAmount)
                .reduce(QuantityAmount.ZERO, QuantityAmount::add);
    }

    private static List<DailyQuantity> createDailyQuantities(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            boolean isFirstWeek
    ) {
        List<PlanDate> planDates = weekPeriod.splitIntoDates();
        List<DailyQuantity> dailyQuantities = new ArrayList<>();

        for (int i = 0; i < planDates.size(); i++) {
            QuantityAmount initialDailyAmount = isFirstWeek && i == 0
                    ? weeklyQuantityAmount.quantityAmount()
                    : QuantityAmount.ZERO;

            DailyQuantity dailyQuantity = DailyQuantity.create(
                    weekPeriod,
                    weeklyQuantityAmount,
                    planDates.get(i).date(),
                    new DailyQuantityAmount(
                            initialDailyAmount,
                            QuantityAmount.ZERO,
                            initialDailyAmount, QuantityMemo.NONE
                    )
            );

            dailyQuantities.add(dailyQuantity);
        }

        return dailyQuantities;
    }

    private static void validateReconstructable(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        validateDailyQuantitiesInWeekPeriod(dailyQuantities, weekPeriod);
        validateWeeklyAmountMatchesDailyAmounts(weeklyQuantityAmount, dailyQuantities);
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
        requireNotEmpty(dailyQuantities);
        requireNonNull(weekPeriod);

        dailyQuantities.forEach(dailyQuantity -> {
            LocalDate date = dailyQuantity.getDate();

            if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
                throw new BadRequestException(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
            }
        });
    }

    private static void validateWeeklyAmountMatchesDailyAmounts(
            WeeklyQuantityAmount weeklyQuantityAmount,
            List<DailyQuantity> dailyQuantities
    ) {
        requireNotEmpty(dailyQuantities);
        requireNonNull(weeklyQuantityAmount);

        QuantityAmount summedDailyAmount = sumDailyQuantityAmount(dailyQuantities);

        if (!weeklyQuantityAmount.quantityAmount().equals(summedDailyAmount)) {
            throw new BadRequestException(WEEKLY_AMOUNT_MISMATCH);
        }
    }

    private void validateWeeklyAmountMatchesDailyAmounts() {
        QuantityAmount summedDailyAmount = sumDailyQuantityAmount(this.dailyQuantities);

        if (!this.weeklyQuantityAmount.quantityAmount().equals(summedDailyAmount)) {
            throw new BadRequestException(WEEKLY_AMOUNT_MISMATCH);
        }
    }

    private void validateReducible(QuantityAmount targetWeeklyAmount) {
        if (targetWeeklyAmount.isGreaterThan(this.weeklyQuantityAmount.quantityAmount())) {
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

    private void validateNotLastDayReduction(QuantityAmount dailyAmountToReduce, int dailyQuantityIndex) {
        if (!dailyAmountToReduce.isZero() && isLastDay(dailyQuantityIndex)) {
            throw new BadRequestException(LAST_DAILY_AMOUNT_REDUCTION_NOT_ALLOWED);
        }
    }

    private static QuantityAmount sumDailyQuantityAmount(List<DailyQuantity> dailyQuantities) {
        return dailyQuantities.stream()
                .map(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().quantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::add);
    }

    private QuantityAmount getDelayedQuantityAmount() {
        return dailyQuantities.stream()
                .map(dailyQuantity -> dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::add);
    }

    private int findDailyQuantityIndex(Long dailyQuantityId) {
        for (int i = 0; i < this.dailyQuantities.size(); i++) {
            if (dailyQuantityId.equals(this.dailyQuantities.get(i).getDailyQuantityId())) {
                return i;
            }
        }

        throw new NotFoundException(DAILY_QUANTITY_NOT_FOUND);
    }

    private void carryOverReductionAmountToNextDay(
            int currentDailyQuantityIndex,
            QuantityAmount dailyAmountToIncrease
    ) {
        if (dailyAmountToIncrease.isZero()) {
            return;
        }

        this.dailyQuantities.get(currentDailyQuantityIndex + 1).increaseBy(dailyAmountToIncrease);
    }

    private boolean isLastDay(int dailyQuantityIndex) {
        return dailyQuantityIndex == this.dailyQuantities.size() - 1;
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
