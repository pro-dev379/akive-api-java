package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import java.time.LocalDate;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_AMOUNT_EXCEEDS_WEEKLY_AMOUNT;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_QUANTITY_DATE_OUT_OF_WEEK;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
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

    QuantityAmount reduceByAsMuchAsPossible(QuantityAmount dailyAmountToReduce) {
        QuantityAmount appliedReductionAmount = QuantityAmount.min(
                dailyAmountToReduce,
                this.dailyQuantityAmount.delayedQuantityAmount()
        );

        this.dailyQuantityAmount = this.dailyQuantityAmount.reduceBy(appliedReductionAmount);

        return appliedReductionAmount;
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
            throw new BadRequestException(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
        }

        if (weeklyQuantityAmount.quantityAmount().isLessThan(dailyQuantityAmount.quantityAmount())) {
            throw new BadRequestException(DAILY_AMOUNT_EXCEEDS_WEEKLY_AMOUNT);
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
