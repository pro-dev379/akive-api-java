package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class TotalQuantity {

    private Long totalQuantityId;
    private Long sessionalPlanId;
    private String subject;
    private String subjectCategory;
    private Integer totalQuantityAmount;
    private String unit;
    private String totalQuantityMemo;
    private List<WeeklyQuantity> weeklyQuantities;

    public static TotalQuantity create(
            Long totalQuantityId,
            Long sessionalPlanId,
            String subject,
            String subjectCategory,
            Integer totalQuantityAmount,
            String unit,
            String totalQuantityMemo
    ) {
        return TotalQuantity.builder()
                .totalQuantityId(totalQuantityId)
                .sessionalPlanId(sessionalPlanId)
                .subject(subject)
                .subjectCategory(subjectCategory)
                .totalQuantityAmount(totalQuantityAmount)
                .unit(unit)
                .totalQuantityMemo(totalQuantityMemo)
                .build();
    }
}
