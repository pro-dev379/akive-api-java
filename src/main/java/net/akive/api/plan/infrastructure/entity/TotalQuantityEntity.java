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

    @Embedded
    private SubjectEntity subject;

    @Embedded
    private TotalQuantityAmountEntity totalQuantityAmount;

    public static TotalQuantityEntity from(TotalQuantity totalQuantity) {
        return new TotalQuantityEntity(
                totalQuantity.getTotalQuantityId(),
                SessionalPlanEntity.from(totalQuantity.getSessionalPlan()),
                SubjectEntity.from(totalQuantity.getSubject()),
                TotalQuantityAmountEntity.from(totalQuantity.getTotalQuantityAmount())
        );
    }

    public TotalQuantity to() {
        return TotalQuantity.create(
                this.totalQuantityId,
                this.sessionalPlan.to(),
                this.subject.to(),
                this.totalQuantityAmount.to()
        );
    }
}
