package net.akive.api.plan.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.value.TotalQuantityAmount;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class TotalQuantityAmountEntity {

    @Column(name = "total_quantity_amount")
    private int quantityAmount;

    @Column(name = "total_quantity_unit", length = 10, nullable = false)
    private String quantityUnit;

    @Column(name = "total_quantity_memo")
    private String quantityMemo;

    public static TotalQuantityAmountEntity from(TotalQuantityAmount totalQuantityAmount) {
        return new TotalQuantityAmountEntity(
                totalQuantityAmount.quantityAmount().value(),
                totalQuantityAmount.quantityUnit().value(),
                totalQuantityAmount.quantityMemo().value()
        );
    }

    public TotalQuantityAmount to() {
        return TotalQuantityAmount.of(
                this.quantityAmount,
                this.quantityUnit,
                this.quantityMemo
        );
    }
}
