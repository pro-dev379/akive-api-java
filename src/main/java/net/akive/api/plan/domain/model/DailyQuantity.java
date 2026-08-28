package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;

import java.time.LocalDate;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_QUANTITY_DATE_OUT_OF_WEEK;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_QUANTITY_EXCEEDS_WEEKLY_QUANTITY;

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
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);
        requireNonNull(date);
        validateDateInWeekPeriod(date, weekPeriod);
        requireNonNull(dailyQuantityAmount);

        validateDailyQuantityAmountInWeeklyQuantityAmount(
                dailyQuantityAmount,
                weeklyQuantityAmount
        );

        return DailyQuantity.builder()
                .date(date)
                .dailyQuantityAmount(dailyQuantityAmount)
                .build();
    }

    void increase(int amount) {
        if (amount == 0) {
            return;
        }

        this.dailyQuantityAmount = new DailyQuantityAmount(
                this.dailyQuantityAmount.quantityAmount().plus(amount),
                this.dailyQuantityAmount.completedQuantityAmount(),
                this.dailyQuantityAmount.delayedQuantityAmount().plus(amount),
                this.dailyQuantityAmount.quantityMemo()
        );
    }

    QuantityReductionResult reduce(int amount) {
        if (amount == 0) {
            return new QuantityReductionResult(0);
        }

        int quantityAmountToReduce = Math.min(amount, this.dailyQuantityAmount.delayedQuantityAmount().value());
        this.dailyQuantityAmount = new DailyQuantityAmount(
                this.dailyQuantityAmount.quantityAmount().minus(quantityAmountToReduce),
                this.dailyQuantityAmount.completedQuantityAmount(),
                this.dailyQuantityAmount.delayedQuantityAmount().minus(quantityAmountToReduce),
                this.dailyQuantityAmount.quantityMemo()
        );

        return new QuantityReductionResult(quantityAmountToReduce);
    }

    int getCompletedQuantityAmount() {
        return this.dailyQuantityAmount.completedQuantityAmount().value();
    }

    private static void validateDateInWeekPeriod(LocalDate date, WeekPeriod weekPeriod) {
        if (date.isBefore(weekPeriod.startOn()) || date.isAfter(weekPeriod.endOn())) {
            throw new BadRequestException(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
        }
    }

    private static void validateDailyQuantityAmountInWeeklyQuantityAmount(
            DailyQuantityAmount dailyQuantityAmount,
            WeeklyQuantityAmount weeklyQuantityAmount
    ) {
        if (weeklyQuantityAmount.quantityAmount().value() < dailyQuantityAmount.quantityAmount().value()) {
            throw new BadRequestException(DAILY_QUANTITY_EXCEEDS_WEEKLY_QUANTITY);
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

    record QuantityReductionResult(int quantityAmountToReduce) {
    }
}
