package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantity {

    private Long weeklyQuantityId;
    private TotalQuantity totalQuantity;
    private WeekPeriod weekPeriod;
    private WeeklyQuantityAmount weeklyQuantityAmount;

    public static WeeklyQuantity createForTotalQuantity(
            TotalQuantity totalQuantity,
            SessionPeriod sessionPeriod,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount
    ) {
        requireNonNull(totalQuantity);
        requireNonNull(sessionPeriod);
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);
        validateWeekPeriodInSessionPeriod(weekPeriod, sessionPeriod);
        validateWeeklyQuantityAmountInTotalQuantityAmount(
                weeklyQuantityAmount,
                totalQuantity.getTotalQuantityAmount()
        );

        return WeeklyQuantity.builder()
                .totalQuantity(totalQuantity)
                .weekPeriod(weekPeriod)
                .weeklyQuantityAmount(weeklyQuantityAmount)
                .build();
    }

    public static WeeklyQuantity create(
            Long weeklyQuantityId,
            TotalQuantity totalQuantity,
            WeekPeriod weekPeriod,
            WeeklyQuantityAmount weeklyQuantityAmount
    ) {
        requireNonNull(totalQuantity);
        requireNonNull(weekPeriod);
        requireNonNull(weeklyQuantityAmount);

        validateWeeklyQuantityAmountInTotalQuantityAmount(
                weeklyQuantityAmount,
                totalQuantity.getTotalQuantityAmount()
        );

        return WeeklyQuantity.builder()
                .weeklyQuantityId(requirePositive(weeklyQuantityId))
                .totalQuantity(requireNonNull(totalQuantity))
                .weekPeriod(requireNonNull(weekPeriod))
                .weeklyQuantityAmount(weeklyQuantityAmount)
                .build();
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
}
