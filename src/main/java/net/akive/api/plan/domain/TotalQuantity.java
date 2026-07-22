package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static TotalQuantity createForUser(
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        return TotalQuantity.builder()
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .build();
    }

    public static TotalQuantity create(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        return TotalQuantity.builder()
                .totalQuantityId(requirePositive(totalQuantityId))
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(requireNonNull(subject))
                .totalQuantityAmount(requireNonNull(totalQuantityAmount))
                .build();
    }
}
