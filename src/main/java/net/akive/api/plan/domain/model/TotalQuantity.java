package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.plan.domain.value.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class TotalQuantity {

    private Long totalQuantityId;

    private SessionalPlan sessionalPlan;

    private Subject subject;

    private TotalQuantityAmount totalQuantityAmount;

    private List<WeeklyQuantity> weeklyQuantities;

    public static TotalQuantity reconstruct(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount,
            List<WeeklyQuantity> weeklyQuantities
    ) {
        return TotalQuantity.builder()
                .totalQuantityId(requirePositive(totalQuantityId))
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .weeklyQuantities(requireNotEmpty(weeklyQuantities))
                .build();
    }

    public static TotalQuantity create(
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        requireNonNull(sessionalPlan);
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);

        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();
        List<WeeklyQuantity> weeklyQuantities = new ArrayList<>();

        for (int i = 0; i < sessionWeeks.size(); i++) {
            boolean firstWeek = i == 0;
            int weeklyAmount = firstWeek ? totalQuantityAmount.quantityAmount().value() : 0;
            WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(weeklyAmount, null);
            WeekPeriod weekPeriod = sessionWeeks.get(i).weekPeriod();

            WeeklyQuantity weeklyQuantity = WeeklyQuantity.create(
                    totalQuantityAmount,
                    sessionalPlan.getSessionPeriod(),
                    weekPeriod,
                    weeklyQuantityAmount,
                    firstWeek
            );

            weeklyQuantities.add(weeklyQuantity);
        }

        return TotalQuantity.builder()
                .sessionalPlan(sessionalPlan)
                .subject(subject)
                .totalQuantityAmount(totalQuantityAmount)
                .weeklyQuantities(List.copyOf(weeklyQuantities))
                .build();
    }

    public TotalQuantity update(
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);
        validateUpdatable(totalQuantityAmount);

        List<WeeklyQuantity> weeklyQuantities = adjustWeeklyQuantities(
                calculateAmountDelta(totalQuantityAmount)
        );

        return TotalQuantity.builder()
                .totalQuantityId(this.totalQuantityId)
                .sessionalPlan(this.sessionalPlan)
                .subject(subject)
                .totalQuantityAmount(totalQuantityAmount)
                .weeklyQuantities(List.copyOf(weeklyQuantities))
                .build();
    }

    private void validateUpdatable(TotalQuantityAmount newTotalQuantityAmount) {
        if (newTotalQuantityAmount.quantityAmount().value()
                < getCompletedQuantityAmount()) {
            throw new ConflictException(TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);
        }
    }

    private int calculateAmountDelta(TotalQuantityAmount newTotalQuantityAmount) {
        return newTotalQuantityAmount.quantityAmount().value()
                - this.totalQuantityAmount.quantityAmount().value();
    }

    private List<WeeklyQuantity> adjustWeeklyQuantities(int amountDelta) {
        if (amountDelta == 0) {
            return this.weeklyQuantities;
        }

        if (amountDelta > 0) {
            return increaseWeeklyQuantity(amountDelta);
        }

        return reduceWeeklyQuantities(Math.abs(amountDelta));
    }

    private List<WeeklyQuantity> increaseWeeklyQuantity(int amountToIncrease) {
        List<WeeklyQuantity> adjustedWeeklyQuantities = new ArrayList<>(this.weeklyQuantities);
        WeeklyQuantity firstWeeklyQuantity = adjustedWeeklyQuantities.getFirst()
                .increaseFirstDailyQuantity(amountToIncrease);

        adjustedWeeklyQuantities.set(0, firstWeeklyQuantity);

        return adjustedWeeklyQuantities;
    }

    private List<WeeklyQuantity> reduceWeeklyQuantities(int amountToReduce) {
        int remainingAmount = amountToReduce;
        List<WeeklyQuantity> adjustedWeeklyQuantities = new ArrayList<>(this.weeklyQuantities);

        for (int i = 0; i < this.weeklyQuantities.size(); i++) {
            WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(i);
            WeeklyQuantity.QuantityReductionResult result = weeklyQuantity.reduceQuantity(remainingAmount);

            adjustedWeeklyQuantities.set(i, result.weeklyQuantity());
            remainingAmount = result.remainingAmount();

            if (remainingAmount == 0) {
                break;
            }
        }

        return adjustedWeeklyQuantities;
    }

    private int getCompletedQuantityAmount() {
        return weeklyQuantities.stream()
                .mapToInt(WeeklyQuantity::getCompletedQuantityAmount)
                .sum();
    }
}
