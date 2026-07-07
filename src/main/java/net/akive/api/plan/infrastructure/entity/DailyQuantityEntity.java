package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.DailyQuantity;
import org.hibernate.annotations.OnDelete;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(
        name = "daily_quantity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-daily_quantity-weekly_quantity_id-date",
                        columnNames = {"weekly_quantity_id", "date"}
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class DailyQuantityEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long dailyQuantityId;

    @OnDelete(action = CASCADE)
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "weekly_quantity_id", nullable = false)
    private WeeklyQuantityEntity weeklyQuantity;

    @Column(nullable = false)
    private LocalDate date;

    private Integer dailyQuantityAmount;

    private Integer completedQuantityAmount;

    private Integer delayQuantityAmount;

    @Column(length = 25)
    private String dailyQuantityMemo;

    public static DailyQuantityEntity fromModel(DailyQuantity dailyQuantity) {
        DailyQuantityEntity dailyQuantityEntity = new DailyQuantityEntity();
        dailyQuantityEntity.dailyQuantityId = dailyQuantity.getDailyQuantityId();
        dailyQuantityEntity.weeklyQuantity = WeeklyQuantityEntity.reference(dailyQuantity.getWeeklyQuantityId());
        dailyQuantityEntity.date = dailyQuantity.getDate();
        dailyQuantityEntity.dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();
        dailyQuantityEntity.completedQuantityAmount = dailyQuantity.getCompletedQuantityAmount();
        dailyQuantityEntity.delayQuantityAmount = dailyQuantity.getDelayQuantityAmount();
        dailyQuantityEntity.dailyQuantityMemo = dailyQuantity.getDailyQuantityMemo();

        return dailyQuantityEntity;
    }

    public DailyQuantity toModel() {
        return DailyQuantity.create(
                this.dailyQuantityId,
                this.weeklyQuantity.getWeeklyQuantityId(),
                this.date,
                this.dailyQuantityAmount,
                this.completedQuantityAmount,
                this.delayQuantityAmount,
                this.dailyQuantityMemo
        );
    }


}
