package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String subject;
    private String subjectCategory;
    private int totalQuantityAmount;
    private String unit;
    private String totalQuantityMemo;
    private List<WeeklyQuantity> weeklyQuantities;

    public static TotalQuantity create(
            Long totalQuantityId,
            SessionalPlan sessionalPlan,
            String subject,
            String subjectCategory,
            int totalQuantityAmount,
            String unit,
            String totalQuantityMemo
    ) {
        return TotalQuantity.builder()
                .totalQuantityId(totalQuantityId)
                .sessionalPlan(requireNonNull(sessionalPlan))
                .subject(validateSubject(subject))
                .subjectCategory(validateSubjectCategory(subjectCategory))
                .totalQuantityAmount(requirePositive(totalQuantityAmount))
                .unit(validateUnit(unit))
                .totalQuantityMemo(requireMaxLengthIfPresent(totalQuantityMemo, 25))
                .build();
    }

    private static String validateSubject(String subject) {
        return requireMaxLength(requireNotBlank(subject), 100);
    }

    private static String validateSubjectCategory(String subjectCategory) {
        return requireMaxLength(requireNotBlank(subjectCategory), 10);
    }

    private static String validateUnit(String unit) {
        return requireMaxLength(requireNotBlank(unit), 10);
    }
}
