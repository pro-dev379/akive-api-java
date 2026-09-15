package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.*;

import java.time.LocalDate;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class DailyQuantity {

    private Long dailyQuantityId;

    private LocalDate date;

    private DailyQuantityAmount dailyQuantityAmount;

    public static DailyQuantity reconstruct(
            Long dailyQuantityId,
            LocalDate date,
            DailyQuantityAmount dailyQuantityAmount
    ) {
        return DailyQuantity.builder()
                .dailyQuantityId(requirePositive(dailyQuantityId))
                .date(requireNonNull(date))
                .dailyQuantityAmount(requireNonNull(dailyQuantityAmount))
                .build();
    }

    static DailyQuantity create(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            LocalDate date,
            DailyQuantityAmount dailyQuantityAmount
    ) {
        validateCreatable(weekPeriod, weeklyQuantityAmount, date, dailyQuantityAmount);

        return DailyQuantity.builder()
                .date(requireNonNull(date))
                .dailyQuantityAmount(requireNonNull(dailyQuantityAmount))
                .build();
    }

    void increaseBy(QuantityAmount dailyAmountToIncrease) {
        if (dailyAmountToIncrease.isZero()) {
            return;
        }

        this.dailyQuantityAmount = this.dailyQuantityAmount.increaseBy(dailyAmountToIncrease);
    }

    void reduceBy(QuantityAmount dailyAmountToReduce) {
        if (dailyAmountToReduce.isZero()) {
            return;
        }

        this.dailyQuantityAmount = this.dailyQuantityAmount.reduceBy(dailyAmountToReduce);
    }

    void updateAmount(QuantityAmount targetDailyAmount, QuantityMemo targetDailyMemo) {
        validateReducible(targetDailyAmount);

        QuantityAmount dailyAmountToReduce = calculateAmountToReduceTo(targetDailyAmount);

        if (dailyAmountToReduce.isZero()) {
            this.dailyQuantityAmount = this.dailyQuantityAmount.updateMemo(targetDailyMemo);
            return;
        }

        this.dailyQuantityAmount = this.dailyQuantityAmount.reduceBy(dailyAmountToReduce, targetDailyMemo);
    }

    void updateCompletedAmount(QuantityAmount targetCompletedAmount) {
        QuantityAmountDelta completedAmountDelta =
                this.dailyQuantityAmount.completedQuantityAmount().deltaTo(targetCompletedAmount);

        if (completedAmountDelta.isZero()) {
            return;
        }

        if (completedAmountDelta.isNegative()) {
            this.dailyQuantityAmount =
                    this.dailyQuantityAmount.cancelCompletionBy(completedAmountDelta.toAbsoluteAmount());
            return;
        }

        this.dailyQuantityAmount =
                this.dailyQuantityAmount.completeBy(completedAmountDelta.toAbsoluteAmount());
    }

    QuantityAmount reduceByAsMuchAsPossible(QuantityAmount dailyAmountToReduce) {
        QuantityAmount appliedReductionAmount = QuantityAmount.min(
                dailyAmountToReduce,
                this.dailyQuantityAmount.delayedQuantityAmount()
        );

        this.dailyQuantityAmount = this.dailyQuantityAmount.reduceBy(appliedReductionAmount);

        return appliedReductionAmount;
    }

    void delayTo(DailyQuantity receivingDailyQuantity, QuantityAmount dailyAmountToDelay) {
        if (dailyAmountToDelay.isGreaterThan(getDailyQuantityAmount().delayedQuantityAmount())) {
            throw new BusinessRuleViolationException(DAILY_DELAY_AMOUNT_EXCEEDS_CURRENT_DAILY_DELAYED_AMOUNT);
        }

        this.reduceBy(dailyAmountToDelay);
        receivingDailyQuantity.increaseBy(dailyAmountToDelay);
    }

    QuantityAmount calculateAmountToReduceTo(QuantityAmount targetDailyAmount) {
        validateReducible(targetDailyAmount);

        return this.dailyQuantityAmount.quantityAmount().subtract(targetDailyAmount);
    }

    QuantityAmount getCompletedAmount() {
        return this.dailyQuantityAmount.completedQuantityAmount();
    }

    private static void validateCreatable(
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount,
            LocalDate date,
            DailyQuantityAmount dailyQuantityAmount
    ) {
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);
        requireNonNull(date);
        requireNonNull(dailyQuantityAmount);

        if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
            throw new BusinessRuleViolationException(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
        }

        if (weeklyQuantityAmount.quantityAmount().isLessThan(dailyQuantityAmount.quantityAmount())) {
            throw new BusinessRuleViolationException(DAILY_AMOUNT_EXCEEDS_WEEKLY_AMOUNT);
        }
    }

    private void validateReducible(QuantityAmount targetDailyAmount) {
        if (targetDailyAmount.isGreaterThan(this.dailyQuantityAmount.quantityAmount())) {
            throw new BusinessRuleViolationException(DAILY_AMOUNT_INCREASE_NOT_ALLOWED);
        }

        if (targetDailyAmount.isLessThan(getCompletedAmount())) {
            throw new BusinessRuleViolationException(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DailyQuantity that = (DailyQuantity) o;

        return this.dailyQuantityId != null && Objects.equals(this.dailyQuantityId, that.dailyQuantityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
