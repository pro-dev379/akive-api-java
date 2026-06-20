package net.akive.api.user.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.user.domain.Goal;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "goal")
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long goalId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private LocalTime attendOn;

    @Column(name = "rest_10min_sec")
    private Integer rest10MinSec;

    private Integer mealNum;

    @Column(name = "meal_1time_sec")
    private Integer meal1TimeSec;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    private void prePersist() {
        createdDate = LocalDateTime.now();
    }

    public static GoalEntity fromModel(Goal goal) {
        GoalEntity goalEntity = new GoalEntity();
        goalEntity.goalId = goal.getGoalId();
        goalEntity.user = UserEntity.reference(goal.getUserId());
        goalEntity.attendOn = goal.getAttendOn();
        goalEntity.rest10MinSec = goal.getRest10MinSec();
        goalEntity.mealNum = goal.getMealNum();

        return goalEntity;
    }

    public Goal toModel() {
        return Goal.builder()
                .goalId(this.goalId)
                .userId(this.user.getUserId())
                .attendOn(this.attendOn)
                .rest10MinSec(this.rest10MinSec)
                .mealNum(this.mealNum)
                .meal1TimeSec(this.meal1TimeSec)
                .build();
    }
}
