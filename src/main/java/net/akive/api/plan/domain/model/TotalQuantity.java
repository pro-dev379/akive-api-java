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
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT;
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
        validateTotalQuantityAmountMatchesWeeklyAmounts(totalQuantityAmount, weeklyQuantities);

        return TotalQuantity.builder()
                .totalQuantityId(requirePositive(totalQuantityId))
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .weeklyQuantities(requireNotEmpty(List.copyOf(weeklyQuantities)))
                .build();
    }

    public static TotalQuantity create(
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        List<WeeklyQuantity> weeklyQuantities = createWeeklyQuantities(sessionalPlan, totalQuantityAmount);

        return TotalQuantity.builder()
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .weeklyQuantities(requireNotEmpty(List.copyOf(weeklyQuantities)))
                .build();
    }

    public void update(Subject targetSubject, TotalQuantityAmount targetTotalQuantityAmount) {
        validateUpdatable(targetSubject, targetTotalQuantityAmount);

        adjustWeeklyQuantitiesTo(targetTotalQuantityAmount.quantityAmount());

        this.subject = requireNonNull(targetSubject);
        this.totalQuantityAmount = requireNonNull(targetTotalQuantityAmount);
    }

    public void updateWeeklyQuantityAmount(
            Long weeklyQuantityId,
            WeeklyQuantityAmount targetWeeklyQuantityAmount
    ) {
        requirePositive(weeklyQuantityId);
        requireNonNull(targetWeeklyQuantityAmount);

        int weeklyQuantityIndex = findWeeklyQuantityIndex(weeklyQuantityId);
        WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(weeklyQuantityIndex);
        QuantityAmount weeklyAmountToReduce =
                weeklyQuantity.calculateAmountToReduceTo(targetWeeklyQuantityAmount.quantityAmount());

        validateNotLastWeekReduction(weeklyQuantityIndex, weeklyAmountToReduce);

        weeklyQuantity.updateQuantityAmount(targetWeeklyQuantityAmount);

        carryOverReductionAmountToNextWeek(weeklyQuantityIndex, weeklyAmountToReduce);
    }

    public void delayWeeklyAmountTo(
            Long weeklyQuantityId,
            Long targetWeeklyQuantityId,
            QuantityAmount weeklyAmountToDelay
    ) {
        requirePositive(weeklyQuantityId);
        requirePositive(targetWeeklyQuantityId);
        requireNonNull(weeklyAmountToDelay);

        if (weeklyQuantityId.equals(targetWeeklyQuantityId)) {
            throw new BadRequestException(WEEKLY_DELAY_TARGET_SAME_AS_SOURCE);
        }

        WeeklyQuantity weeklyQuantity =
                this.weeklyQuantities.get(findWeeklyQuantityIndex(weeklyQuantityId));
        WeeklyQuantity targetWeeklyQuantity =
                this.weeklyQuantities.get(findWeeklyQuantityIndex(targetWeeklyQuantityId));

        weeklyQuantity.delayAmountTo(targetWeeklyQuantity, weeklyAmountToDelay);
    }

    public void updateDailyAmount(
            Long weeklyQuantityId,
            Long dailyQuantityId,
            QuantityAmount targetDailyAmount,
            QuantityMemo targetDailyMemo
    ) {
        requirePositive(weeklyQuantityId);
        requirePositive(dailyQuantityId);
        requireNonNull(targetDailyAmount);

        WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(findWeeklyQuantityIndex(weeklyQuantityId));

        weeklyQuantity.updateDailyAmount(dailyQuantityId, targetDailyAmount, targetDailyMemo);
    }

    public void updateDailyCompletedAmount(
            Long weeklyQuantityId,
            Long dailyQuantityId,
            QuantityAmount targetDailyCompletedAmount
    ) {
        requirePositive(weeklyQuantityId);
        requirePositive(dailyQuantityId);
        requireNonNull(targetDailyCompletedAmount);

        WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(findWeeklyQuantityIndex(weeklyQuantityId));

        weeklyQuantity.updateDailyCompletedAmount(dailyQuantityId, targetDailyCompletedAmount);
    }

    private static List<WeeklyQuantity> createWeeklyQuantities(
            SessionalPlan sessionalPlan,
            TotalQuantityAmount totalQuantityAmount
    ) {
        requireNonNull(sessionalPlan);
        requireNonNull(totalQuantityAmount);

        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();
        List<WeeklyQuantity> weeklyQuantities = new ArrayList<>();

        for (int i = 0; i < sessionWeeks.size(); i++) {
            boolean isFirstWeek = i == 0;
            QuantityAmount initialWeeklyAmount = isFirstWeek
                    ? totalQuantityAmount.quantityAmount()
                    : QuantityAmount.ZERO;
            WeeklyQuantityAmount weeklyQuantityAmount = new WeeklyQuantityAmount(
                    initialWeeklyAmount,
                    QuantityMemo.NONE
            );
            WeekPeriod weekPeriod = sessionWeeks.get(i).weekPeriod();

            WeeklyQuantity weeklyQuantity = WeeklyQuantity.create(
                    totalQuantityAmount,
                    sessionalPlan.getSessionPeriod(),
                    weekPeriod,
                    weeklyQuantityAmount,
                    isFirstWeek
            );

            weeklyQuantities.add(weeklyQuantity);
        }

        return weeklyQuantities;
    }

    private static void validateTotalQuantityAmountMatchesWeeklyAmounts(
            TotalQuantityAmount totalQuantityAmount,
            List<WeeklyQuantity> weeklyQuantities
    ) {
        requireNotEmpty(weeklyQuantities);
        requireNonNull(totalQuantityAmount);

        QuantityAmount summedWeeklyAmount = weeklyQuantities.stream()
                .map(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount().quantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::add);

        if (!totalQuantityAmount.quantityAmount().equals(summedWeeklyAmount)) {
            throw new BadRequestException(TOTAL_AMOUNT_MISMATCH);
        }
    }

    private void validateUpdatable(Subject targetSubject, TotalQuantityAmount targetTotalQuantityAmount) {
        requireNonNull(targetSubject);
        requireNonNull(targetTotalQuantityAmount);

        if (targetTotalQuantityAmount.quantityAmount().isLessThan(getCompletedAmount())) {
            throw new ConflictException(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }
    }

    private void adjustWeeklyQuantitiesTo(QuantityAmount targetTotalAmount) {
        QuantityAmountDelta totalAmountDelta = this.totalQuantityAmount.quantityAmount().deltaTo(targetTotalAmount);

        if (totalAmountDelta.isZero()) {
            return;
        }

        if (totalAmountDelta.isPositive()) {
            this.weeklyQuantities.getFirst().increaseBy(totalAmountDelta.toAbsoluteAmount());
            return;
        }

        reduceWeeklyAmountsBy(totalAmountDelta.toAbsoluteAmount());
    }

    private void reduceWeeklyAmountsBy(QuantityAmount totalAmountToReduce) {
        QuantityAmount remainingAmountToReduce = totalAmountToReduce;

        for (WeeklyQuantity weeklyQuantity : this.weeklyQuantities) {
            remainingAmountToReduce = weeklyQuantity.reduceDailyAmountsBy(remainingAmountToReduce);

            if (remainingAmountToReduce.isZero()) {
                break;
            }
        }
    }

    private void carryOverReductionAmountToNextWeek(
            int weeklyQuantityIndex,
            QuantityAmount weeklyAmountToIncrease
    ) {
        if (weeklyAmountToIncrease.isZero()) {
            return;
        }

        this.weeklyQuantities.get(weeklyQuantityIndex + 1).increaseBy(weeklyAmountToIncrease);
    }

    private QuantityAmount getCompletedAmount() {
        return weeklyQuantities.stream()
                .map(WeeklyQuantity::getCompletedAmount)
                .reduce(QuantityAmount.ZERO, QuantityAmount::add);
    }

    private int findWeeklyQuantityIndex(Long weeklyQuantityId) {
        for (int i = 0; i < this.weeklyQuantities.size(); i++) {
            if (weeklyQuantityId.equals(this.weeklyQuantities.get(i).getWeeklyQuantityId())) {
                return i;
            }
        }

        throw new NotFoundException(WEEKLY_QUANTITY_NOT_FOUND);
    }

    private void validateNotLastWeekReduction(
            int weeklyQuantityIndex,
            QuantityAmount weeklyAmountToReduce
    ) {
        if (!weeklyAmountToReduce.isZero() && isLastWeek(weeklyQuantityIndex)) {
            throw new BadRequestException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);
        }
    }

    private boolean isLastWeek(int weeklyQuantityIndex) {
        return weeklyQuantityIndex == this.weeklyQuantities.size() - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TotalQuantity that = (TotalQuantity) o;

        return this.totalQuantityId != null && Objects.equals(this.totalQuantityId, that.totalQuantityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
