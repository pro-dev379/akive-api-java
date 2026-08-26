package net.akive.api.plan.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.value.DailyQuantityAmount;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class DailyQuantityAmountEntity {

    @Column(name = "daily_quantity_amount")
    private int quantityAmount;

    private int completedQuantityAmount;

    private int delayedQuantityAmount;

    @Column(name = "daily_quantity_memo")
    private String quantityMemo;

    public static DailyQuantityAmountEntity from(DailyQuantityAmount dailyQuantityAmount) {
        return new DailyQuantityAmountEntity(
                dailyQuantityAmount.quantityAmount().value(),
                dailyQuantityAmount.completedQuantityAmount().value(),
                dailyQuantityAmount.delayedQuantityAmount().value(),
                dailyQuantityAmount.quantityMemo().value()
        );
    }

    public DailyQuantityAmount to() {
        return DailyQuantityAmount.of(
                this.quantityAmount,
                this.completedQuantityAmount,
                this.delayedQuantityAmount,
                this.quantityMemo
        );
    }
}
