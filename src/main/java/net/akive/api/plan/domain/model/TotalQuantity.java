package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.value.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;

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
}
