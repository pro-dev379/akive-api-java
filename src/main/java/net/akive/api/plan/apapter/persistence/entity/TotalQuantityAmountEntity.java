package net.akive.api.plan.apapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.plan.domain.TotalQuantityAmount;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class TotalQuantityAmountEntity {

    @Column(name = "total_quantity_amount")
    private int quantityAmount;

    @Column(length = 10, nullable = false)
    private String unit;

    @Column(name = "total_quantity_memo")
    private String memo;

    public static TotalQuantityAmountEntity from(TotalQuantityAmount totalQuantityAmount) {
        return new TotalQuantityAmountEntity(
                totalQuantityAmount.quantityAmount().value(),
                totalQuantityAmount.unit(),
                totalQuantityAmount.quantityMemo().value()
        );
    }

    public TotalQuantityAmount to() {
        return TotalQuantityAmount.of(
                this.quantityAmount,
                this.unit,
                this.memo
        );
    }
}
