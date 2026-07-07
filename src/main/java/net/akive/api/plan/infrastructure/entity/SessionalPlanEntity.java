package net.akive.api.plan.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.user.infrastructure.entity.UserEntity;

import java.time.LocalDate;

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

    @Column(length = 100, nullable = false)
    private String sessionName;

    @Column(nullable = false)
    private LocalDate sessionStartOn;

    @Column(nullable = false)
    private LocalDate sessionEndOn;

    public static SessionalPlanEntity reference(Long sessionalPlanId) {
        SessionalPlanEntity sessionalPlanEntity = new SessionalPlanEntity();
        sessionalPlanEntity.sessionalPlanId = sessionalPlanId;

        return sessionalPlanEntity;
    }

    public static SessionalPlanEntity fromModel(SessionalPlan sessionalPlan) {
        SessionalPlanEntity sessionalPlanEntity = new SessionalPlanEntity();
        sessionalPlanEntity.sessionalPlanId = sessionalPlan.getSessionalPlanId();
        sessionalPlanEntity.user = UserEntity.reference(sessionalPlan.getUserId());
        sessionalPlanEntity.sessionName = sessionalPlan.getSessionName();
        sessionalPlanEntity.sessionStartOn = sessionalPlan.getSessionStartOn();
        sessionalPlanEntity.sessionEndOn = sessionalPlan.getSessionEndOn();

        return sessionalPlanEntity;
    }

    public SessionalPlan toModel() {
        return SessionalPlan.create(
                this.sessionalPlanId,
                this.user.getUserId(),
                this.sessionName,
                this.sessionStartOn,
                this.sessionEndOn
        );
    }
}