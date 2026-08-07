package net.akive.api.mentoring.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.adapter.persistence.entity.UserEntity;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "mentoring_profile",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-mentoring_profile-user_id",
                        columnNames = "user_id"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class MentoringProfileEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long mentoringProfileId;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String goal;
    private String preference;

    @Column(name = "`constraint`")
    private String constraint;

    private String weakness;
    private String strength;
    private String recurringPattern;
    private String decisionOrCommitment;
    private String studyPlan;
    private String behavioralPattern;
    private String effectiveAdvice;

    public static MentoringProfileEntity from(MentoringProfile mentoringProfile) {
        return new MentoringProfileEntity(
                mentoringProfile.getMentoringProfileId(),
                UserEntity.reference(mentoringProfile.getUserId()),
                mentoringProfile.getGoal(),
                mentoringProfile.getPreference(),
                mentoringProfile.getConstraint(),
                mentoringProfile.getWeakness(),
                mentoringProfile.getStrength(),
                mentoringProfile.getRecurringPattern(),
                mentoringProfile.getDecisionOrCommitment(),
                mentoringProfile.getStudyPlan(),
                mentoringProfile.getBehavioralPattern(),
                mentoringProfile.getEffectiveAdvice()
        );
    }

    public MentoringProfile to() {
        return MentoringProfile.create(
                this.mentoringProfileId,
                this.getUser().getUserId(),
                this.goal,
                this.preference,
                this.constraint,
                this.weakness,
                this.strength,
                this.recurringPattern,
                this.decisionOrCommitment,
                this.studyPlan,
                this.behavioralPattern,
                this.effectiveAdvice
        );
    }
}
