package net.akive.api.plan.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.model.TotalQuantity;
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
        name = "total_quantity",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-total_quantity-sessional_plan_id-subject",
                        columnNames = {"sessional_plan_id", "subject"}
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class TotalQuantityEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long totalQuantityId;

    @OnDelete(action = CASCADE)
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "sessional_plan_id", nullable = false)
    private SessionalPlanEntity sessionalPlan;

    @Embedded
    private SubjectEntity subject;

    @Embedded
    private TotalQuantityAmountEntity totalQuantityAmount;

    @OrderBy("weekPeriod.startOn ASC")
    @OneToMany(mappedBy = "totalQuantity", cascade = ALL, orphanRemoval = true)
    private List<WeeklyQuantityEntity> weeklyQuantities = new ArrayList<>();

    public static TotalQuantityEntity from(TotalQuantity totalQuantity) {
        TotalQuantityEntity totalQuantityEntity = new TotalQuantityEntity(
                totalQuantity.getTotalQuantityId(),
                SessionalPlanEntity.from(totalQuantity.getSessionalPlan()),
                SubjectEntity.from(totalQuantity.getSubject()),
                TotalQuantityAmountEntity.from(totalQuantity.getTotalQuantityAmount()),
                new ArrayList<>()
        );

        totalQuantity.getWeeklyQuantities()
                .forEach(weeklyQuantity -> totalQuantityEntity.addWeeklyQuantity(
                        WeeklyQuantityEntity.from(weeklyQuantity)
                ));

        return totalQuantityEntity;
    }

    public TotalQuantity to() {
        return TotalQuantity.reconstruct(
                this.totalQuantityId,
                this.sessionalPlan.to(),
                this.subject.to(),
                this.totalQuantityAmount.to(),
                this.weeklyQuantities.stream().map(WeeklyQuantityEntity::to).collect(Collectors.toList())
        );
    }

    private void addWeeklyQuantity(WeeklyQuantityEntity weeklyQuantity) {
        weeklyQuantity.assignTotalQuantity(this);

        this.weeklyQuantities.add(weeklyQuantity);
    }
}
