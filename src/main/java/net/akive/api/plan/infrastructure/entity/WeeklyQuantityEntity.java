package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.WeeklyQuantity;
import org.hibernate.annotations.OnDelete;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(
        name = "weekly_quantity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-weekly_quantity-total_quantity_id-week_start_on-week_end_on",
                        columnNames = {"total_quantity_id", "week_start_on", "week_end_on"}
                )
        }
)
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantityEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long weeklyQuantityId;

    @OnDelete(action = CASCADE)
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "total_quantity_id", nullable = false)
    private TotalQuantityEntity totalQuantity;

    @Embedded
    private WeekPeriodEntity weekPeriod;

    @Embedded
    private WeeklyQuantityAmountEntity weeklyQuantityAmount;

    public static WeeklyQuantityEntity from(WeeklyQuantity weeklyQuantity) {
        return new WeeklyQuantityEntity(
                weeklyQuantity.getWeeklyQuantityId(),
                TotalQuantityEntity.from(weeklyQuantity.getTotalQuantity()),
                WeekPeriodEntity.from(weeklyQuantity.getWeekPeriod()),
                WeeklyQuantityAmountEntity.from(weeklyQuantity.getWeeklyQuantityAmount())
        );
    }

    public WeeklyQuantity to() {
        return WeeklyQuantity.create(
                this.weeklyQuantityId,
                this.totalQuantity.to(),
                this.weekPeriod.to(),
                this.weeklyQuantityAmount.to()
        );
    }
}
