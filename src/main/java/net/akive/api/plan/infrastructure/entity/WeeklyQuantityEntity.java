package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.WeeklyQuantity;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "weekly_quantity")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class WeeklyQuantityEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long weeklyQuantityId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "total_quantity_id", nullable = false)
    private TotalQuantityEntity totalQuantity;

    @Column(nullable = false)
    private LocalDate weekStartOn;

    @Column(nullable = false)
    private LocalDate weekEndOn;

    private Integer weeklyQuantityAmount;

    @Column(length = 25)
    private String weeklyQuantityMemo;

    public static WeeklyQuantityEntity reference(Long weeklyQuantityId) {
        WeeklyQuantityEntity weeklyQuantityEntity = new WeeklyQuantityEntity();
        weeklyQuantityEntity.weeklyQuantityId = weeklyQuantityId;

        return weeklyQuantityEntity;
    }

    public static WeeklyQuantityEntity fromModel(WeeklyQuantity weeklyQuantity) {
        WeeklyQuantityEntity weeklyQuantityEntity = new WeeklyQuantityEntity();
        weeklyQuantityEntity.weeklyQuantityId = weeklyQuantity.getWeeklyQuantityId();
        weeklyQuantityEntity.totalQuantity = TotalQuantityEntity.reference(weeklyQuantity.getTotalQuantityId());
        weeklyQuantityEntity.weekStartOn = weeklyQuantity.getWeekStartOn();
        weeklyQuantityEntity.weekEndOn = weeklyQuantity.getWeekEndOn();
        weeklyQuantityEntity.weeklyQuantityAmount = weeklyQuantity.getWeeklyQuantityAmount();
        weeklyQuantityEntity.weeklyQuantityMemo = weeklyQuantity.getWeeklyQuantityMemo();

        return weeklyQuantityEntity;
    }

    public WeeklyQuantity toModel() {
        return WeeklyQuantity.create(
                this.weeklyQuantityId,
                this.totalQuantity.getTotalQuantityId(),
                this.weekStartOn,
                this.weekEndOn,
                this.weeklyQuantityAmount,
                this.weeklyQuantityMemo
        );
    }
}
