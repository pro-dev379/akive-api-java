package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.TotalQuantity;
import net.akive.api.plan.domain.WeeklyQuantity;
import org.hibernate.annotations.OnDelete;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = "weekly_quantity")
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

    private int weeklyQuantityAmount;

    @Column(length = 25)
    private String weeklyQuantityMemo;

    public static WeeklyQuantityEntity fromModel(WeeklyQuantity weeklyQuantity) {
        WeeklyQuantityEntity weeklyQuantityEntity = new WeeklyQuantityEntity();
        weeklyQuantityEntity.weeklyQuantityId = weeklyQuantity.getWeeklyQuantityId();
        weeklyQuantityEntity.totalQuantity = TotalQuantityEntity.fromModel(weeklyQuantity.getTotalQuantity());
        weeklyQuantityEntity.weekPeriod = WeekPeriodEntity.fromModel(weeklyQuantity.getWeekPeriod());
        weeklyQuantityEntity.weeklyQuantityAmount = weeklyQuantity.getWeeklyQuantityAmount();
        weeklyQuantityEntity.weeklyQuantityMemo = weeklyQuantity.getWeeklyQuantityMemo();

        return weeklyQuantityEntity;
    }

    public WeeklyQuantity toModel() {
        TotalQuantity totalQuantity = this.totalQuantity.toModel();

        return WeeklyQuantity.create(
                this.weeklyQuantityId,
                totalQuantity,
                this.weekPeriod.toModel(),
                this.weeklyQuantityAmount,
                this.weeklyQuantityMemo
        );
    }
}
