package net.akive.api.plan.domain;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantity {

    private Long weeklyQuantityId;
    private Long totalQuantityId;
    private LocalDate weekStartOn;
    private LocalDate weekEndOn;
    private Integer weeklyQuantityAmount;
    private String weeklyQuantityMemo;
    private List<DailyQuantity> dailyQuantities;

    public static WeeklyQuantity create(
            Long weeklyQuantityId,
            Long totalQuantityId,
            LocalDate weekStartOn,
            LocalDate weekEndOn,
            Integer weeklyQuantityAmount,
            String weeklyQuantityMemo
    ) {
        return WeeklyQuantity.builder()
                .weeklyQuantityId(weeklyQuantityId)
                .totalQuantityId(totalQuantityId)
                .weekStartOn(weekStartOn)
                .weekEndOn(weekEndOn)
                .weeklyQuantityAmount(weeklyQuantityAmount)
                .weeklyQuantityMemo(weeklyQuantityMemo)
                .build();
    }
}
