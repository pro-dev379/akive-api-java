package net.akive.api.plan.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "sessional_plan",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-sessional_plan-user_id-session_name",
                        columnNames = {"user_id", "session_name"}
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class SessionalPlanEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long sessionalPlanId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String sessionName;

    @Embedded
    private SessionPeriodEntity sessionPeriod;

    public static SessionalPlanEntity from(SessionalPlan sessionalPlan) {
        return new SessionalPlanEntity(
                sessionalPlan.getSessionalPlanId(),
                UserEntity.from(sessionalPlan.getUser()),
                sessionalPlan.getSessionName(),
                SessionPeriodEntity.from(sessionalPlan.getSessionPeriod())
        );
    }

    public SessionalPlan to() {
        try {
            return SessionalPlan.reconstruct(
                    this.sessionalPlanId,
                    this.user.to(),
                    this.sessionName,
                    this.sessionPeriod.to()
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct SessionalPlan: " + this.sessionalPlanId,
                    e
            );
        }
    }
}
