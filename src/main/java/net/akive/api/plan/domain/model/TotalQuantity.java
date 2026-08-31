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

    public void update(
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        validateUpdatable(subject, totalQuantityAmount);

        adjustWeeklyQuantitiesTo(totalQuantityAmount.quantityAmount());

        this.subject = requireNonNull(subject);
        this.totalQuantityAmount = requireNonNull(totalQuantityAmount);
    }

    public void reduceWeeklyQuantity(Long weeklyQuantityId, WeeklyQuantityAmount targetWeeklyQuantityAmount) {
        requirePositive(weeklyQuantityId);
        requireNonNull(targetWeeklyQuantityAmount);

        int weeklyQuantityIndex = findWeeklyQuantityIndex(weeklyQuantityId);
        WeeklyQuantity weeklyQuantity = this.weeklyQuantities.get(weeklyQuantityIndex);
        QuantityAmountDelta weeklyAmountDelta =
                weeklyQuantity.calculateAmountDeltaTo(targetWeeklyQuantityAmount.quantityAmount());

        validateNotLastWeekReduction(weeklyQuantityIndex, weeklyAmountDelta);

        weeklyQuantity.reduce(targetWeeklyQuantityAmount);
        carryOverReducedAmountToNextWeek(weeklyQuantityIndex, weeklyAmountDelta);
    }

    public void delayWeeklyQuantity(
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

        weeklyQuantity.delayTo(targetWeeklyQuantity, weeklyAmountToDelay);
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
            QuantityAmount weeklyAmount = isFirstWeek
                    ? totalQuantityAmount.quantityAmount()
                    : QuantityAmount.ZERO;
            WeeklyQuantityAmount weeklyQuantityAmount = new WeeklyQuantityAmount(
                    weeklyAmount,
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

    private void validateUpdatable(Subject subject, TotalQuantityAmount totalQuantityAmount) {
        requireNonNull(subject);
        requireNonNull(totalQuantityAmount);

        if (totalQuantityAmount.quantityAmount().isLessThan(getCompletedAmount())) {
            throw new ConflictException(TOTAL_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }
    }

    private QuantityAmount getCompletedAmount() {
        return weeklyQuantities.stream()
                .map(WeeklyQuantity::getCompletedAmount)
                .reduce(QuantityAmount.ZERO, QuantityAmount::plus);
    }

    private static void validateTotalQuantityAmountMatchesWeeklyAmounts(
            TotalQuantityAmount totalQuantityAmount,
            List<WeeklyQuantity> weeklyQuantities
    ) {
        requireNotEmpty(weeklyQuantities);
        requireNonNull(totalQuantityAmount);

        QuantityAmount summedWeeklyAmount = weeklyQuantities.stream()
                .map(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount().quantityAmount())
                .reduce(QuantityAmount.ZERO, QuantityAmount::plus);

        if (!totalQuantityAmount.quantityAmount().equals(summedWeeklyAmount)) {
            throw new BadRequestException(TOTAL_AMOUNT_MISMATCH);
        }
    }

    private void adjustWeeklyQuantitiesTo(QuantityAmount targetTotalAmount) {
        QuantityAmountDelta totalAmountDelta =
                this.totalQuantityAmount
                        .quantityAmount()
                        .deltaTo(targetTotalAmount);

        if (totalAmountDelta.isZero()) {
            return;
        }

        if (totalAmountDelta.isPositive()) {
            this.weeklyQuantities.getFirst().increase(totalAmountDelta.absoluteAmount());
            return;
        }

        reduceWeeklyQuantities(totalAmountDelta.absoluteAmount());
    }

    private void reduceWeeklyQuantities(QuantityAmount totalAmountToReduce) {
        QuantityAmount remainingAmountToReduce = totalAmountToReduce;

        for (WeeklyQuantity weeklyQuantity : this.weeklyQuantities) {
            remainingAmountToReduce = weeklyQuantity.reduceDailyQuantities(remainingAmountToReduce);

            if (remainingAmountToReduce.isZero()) {
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

    private void validateNotLastWeekReduction(
            int weeklyQuantityIndex,
            QuantityAmountDelta weeklyAmountDelta
    ) {
        if (weeklyAmountDelta.isNegative() && isLastWeek(weeklyQuantityIndex)) {
            throw new BadRequestException(LAST_WEEKLY_QUANTITY_REDUCTION_NOT_ALLOWED);
        }
    }

    private void carryOverReducedAmountToNextWeek(
            int weeklyQuantityIndex,
            QuantityAmountDelta weeklyAmountDelta
    ) {
        if (weeklyAmountDelta.isNegative()) {
            this.weeklyQuantities.get(weeklyQuantityIndex + 1).increase(weeklyAmountDelta.absoluteAmount());
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
