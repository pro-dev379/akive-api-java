package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.TotalQuantity;
import org.hibernate.annotations.OnDelete;

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

    @Column(length = 100, nullable = false)
    private String subject;

    @Column(length = 10, nullable = false)
    private String subjectCategory;

    private int totalQuantityAmount;

    @Column(length = 10, nullable = false)
    private String unit;

    @Column(length = 25)
    private String totalQuantityMemo;

    public static TotalQuantityEntity from(TotalQuantity totalQuantity) {
        TotalQuantityEntity totalQuantityEntity = new TotalQuantityEntity();
        totalQuantityEntity.totalQuantityId = totalQuantity.getTotalQuantityId();
        totalQuantityEntity.sessionalPlan = SessionalPlanEntity.from(totalQuantity.getSessionalPlan());
        totalQuantityEntity.subject = totalQuantity.getSubject();
        totalQuantityEntity.subjectCategory = totalQuantity.getSubjectCategory();
        totalQuantityEntity.totalQuantityAmount = totalQuantity.getTotalQuantityAmount();
        totalQuantityEntity.unit = totalQuantity.getUnit();
        totalQuantityEntity.totalQuantityMemo = totalQuantity.getTotalQuantityMemo();

        return totalQuantityEntity;
    }

    public TotalQuantity to() {
        return TotalQuantity.create(
                this.totalQuantityId,
                this.sessionalPlan.to(),
                this.subject,
                this.subjectCategory,
                this.totalQuantityAmount,
                this.unit,
                this.totalQuantityMemo
        );
    }
}
