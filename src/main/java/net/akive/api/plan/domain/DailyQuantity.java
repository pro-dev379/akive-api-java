package net.akive.api.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class DailyQuantity {

    private Long dailyQuantityId;
    private Long weeklyQuantityId;
    private LocalDate date;
    private Integer dailyQuantityAmount;
    private Integer completedQuantityAmount;
    private Integer delayQuantityAmount;
    private String dailyQuantityMemo;

    public static DailyQuantity create(
            Long dailyQuantityId,
            Long weeklyQuantityId,
            LocalDate date,
            Integer dailyQuantityAmount,
            Integer completedQuantityAmount,
            Integer delayQuantityAmount,
            String dailyQuantityMemo
    ) {
        return DailyQuantity.builder()
                .dailyQuantityId(dailyQuantityId)
                .weeklyQuantityId(weeklyQuantityId)
                .date(date)
                .dailyQuantityAmount(dailyQuantityAmount)
                .completedQuantityAmount(completedQuantityAmount)
                .delayQuantityAmount(delayQuantityAmount)
                .dailyQuantityMemo(dailyQuantityMemo)
                .build();
    }
}
