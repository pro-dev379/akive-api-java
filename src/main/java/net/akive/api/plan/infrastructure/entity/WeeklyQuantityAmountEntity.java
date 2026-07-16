package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.WeeklyQuantityAmount;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantityAmountEntity {

    @Column(name = "weekly_quantity_amount")
    private int quantityAmount;

    @Column(name = "weekly_quantity_memo", length = 25)
    private String quantityMemo;

    public static WeeklyQuantityAmountEntity from(WeeklyQuantityAmount weeklyQuantityAmount) {
        return new WeeklyQuantityAmountEntity(
                weeklyQuantityAmount.quantityAmount().value(),
                weeklyQuantityAmount.quantityMemo().value()
        );
    }

    public WeeklyQuantityAmount to() {
        return WeeklyQuantityAmount.of(
                this.quantityAmount,
                this.quantityMemo
        );
    }
}
