package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.domain.value.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.TOTAL_QUANTITY_AMOUNT_MISMATCH;
import static net.akive.api.common.exception.business.BadRequestError.LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY;
import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_QUANTITY_NOT_FOUND;

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
        requireNotEmpty(weeklyQuantities);
        validateTotalQuantityAmountMatchesWeeklyQuantities(totalQuantityAmount, weeklyQuantities);

        return TotalQuantity.builder()
                .totalQuantityId(requirePositive(totalQuantityId))
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .weeklyQuantities(List.copyOf(weeklyQuantities))
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

    public void update(
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);
        validateUpdatable(totalQuantityAmount);

        int amountDelta = calculateAmountDelta(totalQuantityAmount);
        adjustWeeklyQuantities(amountDelta);

        this.subject = subject;
        this.totalQuantityAmount = totalQuantityAmount;
    }

    public void reduceWeeklyQuantity(
            Long weeklyQuantityId,
            WeeklyQuantityAmount reducedWeeklyQuantityAmount
    ) {
        requirePositive(weeklyQuantityId);
        requireNonNull(reducedWeeklyQuantityAmount);

        int weeklyQuantityIndex = findWeeklyQuantityIndex(weeklyQuantityId);
        WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(weeklyQuantityIndex);
        int quantityAmountToReduce = weeklyQuantity.calculateReductionAmount(reducedWeeklyQuantityAmount);

        validateNotLastWeekReduction(weeklyQuantityIndex, quantityAmountToReduce);

        WeeklyQuantity.QuantityReductionResult result = weeklyQuantity.reduce(reducedWeeklyQuantityAmount);
        carryOverReducedAmountToNextWeek(weeklyQuantityIndex, result.quantityAmountToReduce());
    }

    private void validateUpdatable(TotalQuantityAmount newTotalQuantityAmount) {
        if (newTotalQuantityAmount.quantityAmount().value() < getCompletedQuantityAmount()) {
            throw new ConflictException(TOTAL_QUANTITY_LESS_THAN_COMPLETED_QUANTITY);
        }
    }

    private int getCompletedQuantityAmount() {
        return weeklyQuantities.stream().mapToInt(WeeklyQuantity::getCompletedQuantityAmount).sum();
    }

    private static void validateTotalQuantityAmountMatchesWeeklyQuantities(
            TotalQuantityAmount totalQuantityAmount,
            List<WeeklyQuantity> weeklyQuantities
    ) {
        requireNonNull(totalQuantityAmount);

        int weeklyQuantityAmount = weeklyQuantities.stream()
                .mapToInt(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value())
                .sum();

        if (totalQuantityAmount.quantityAmount().value() != weeklyQuantityAmount) {
            throw new BadRequestException(TOTAL_QUANTITY_AMOUNT_MISMATCH);
        }
    }

    private int calculateAmountDelta(TotalQuantityAmount newTotalQuantityAmount) {
        return newTotalQuantityAmount.quantityAmount().value()
                - this.totalQuantityAmount.quantityAmount().value();
    }

    private void adjustWeeklyQuantities(int amountDelta) {
        if (amountDelta == 0) {
            return;
        }

        if (amountDelta > 0) {
            this.weeklyQuantities.getFirst().increase(amountDelta);
            return;
        }

        reduceWeeklyQuantities(Math.abs(amountDelta));
    }

    private void reduceWeeklyQuantities(int quantityAmountToReduce) {
        int remainingAmount = quantityAmountToReduce;

        for (WeeklyQuantity weeklyQuantity : this.weeklyQuantities) {
            WeeklyQuantity.QuantityReductionResult result = weeklyQuantity.reduceDailyQuantities(remainingAmount);

            remainingAmount = result.remainingAmount();

            if (remainingAmount == 0) {
                break;
            }
        }
    }

    private int findWeeklyQuantityIndex(Long weeklyQuantityId) {
        for (int i = 0; i < this.weeklyQuantities.size(); i++) {
            if (weeklyQuantityId.equals(this.weeklyQuantities.get(i).getWeeklyQuantityId())) {
                return i;
            }
        }

        throw new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND);
    }

    private void validateNotLastWeekReduction(int weeklyQuantityIndex, int quantityAmountToReduce) {
        if (quantityAmountToReduce > 0 && isLastWeek(weeklyQuantityIndex)) {
            throw new BadRequestException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);
        }
    }

    private void carryOverReducedAmountToNextWeek(int weeklyQuantityIndex, int quantityAmountToReduce) {
        if (quantityAmountToReduce > 0) {
            this.weeklyQuantities.get(weeklyQuantityIndex + 1).increase(quantityAmountToReduce);
        }
    }

    private boolean isLastWeek(int weeklyQuantityIndex) {
        return weeklyQuantityIndex == this.weeklyQuantities.size() - 1;
    }
}
