package net.akive.api.user.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.user.domain.Goal;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "goal")
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long goalId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalTime attendOn;

    @Column(name = "rest_10min_sec")
    private long rest10MinSec;

    private int mealNum;

    @Column(name = "meal_1time_sec")
    private long meal1TimeSec;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static GoalEntity from(Goal goal) {
        GoalEntity goalEntity = new GoalEntity();

        goalEntity.goalId = goal.getGoalId();
        goalEntity.user = UserEntity.from(goal.getUser());
        goalEntity.attendOn = goal.getAttendOn();
        goalEntity.rest10MinSec = goal.getRest10MinSec();
        goalEntity.mealNum = goal.getMealNum();
        goalEntity.meal1TimeSec = goal.getMeal1TimeSec();

        return goalEntity;
    }

    public Goal to() {
        try {
            return Goal.reconstruct(
                    this.goalId,
                    this.user.to(),
                    this.attendOn,
                    this.rest10MinSec,
                    this.mealNum,
                    this.meal1TimeSec
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException("Failed to reconstruct Goal: " + this.goalId, e);
        }
    }
}
