package net.akive.api.plan.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import org.hibernate.annotations.OnDelete;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
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

    @OrderBy("date ASC")
    @OneToMany(mappedBy = "weeklyQuantity", cascade = ALL, orphanRemoval = true)
    private List<DailyQuantityEntity> dailyQuantities = new ArrayList<>();

    public static WeeklyQuantityEntity from(WeeklyQuantity weeklyQuantity) {
        WeeklyQuantityEntity weeklyQuantityEntity = new WeeklyQuantityEntity(
                weeklyQuantity.getWeeklyQuantityId(),
                null,
                WeekPeriodEntity.from(weeklyQuantity.getWeekPeriod()),
                WeeklyQuantityAmountEntity.from(weeklyQuantity.getWeeklyQuantityAmount()),
                new ArrayList<>()
        );

        weeklyQuantity.getDailyQuantities()
                .forEach(dailyQuantity -> weeklyQuantityEntity.addDailyQuantity(
                        DailyQuantityEntity.from(dailyQuantity)
                ));

        return weeklyQuantityEntity;
    }

    public WeeklyQuantity to() {
        return WeeklyQuantity.reconstruct(
                this.weeklyQuantityId,
                this.weekPeriod.to(),
                this.weeklyQuantityAmount.to(),
                this.dailyQuantities.stream().map(DailyQuantityEntity::to).collect(Collectors.toList())
        );
    }

    void assignTotalQuantity(TotalQuantityEntity totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    private void addDailyQuantity(DailyQuantityEntity dailyQuantity) {
        dailyQuantity.assignWeeklyQuantity(this);

        this.dailyQuantities.add(dailyQuantity);
    }
}
