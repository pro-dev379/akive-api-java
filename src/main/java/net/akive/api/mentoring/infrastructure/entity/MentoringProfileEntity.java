package net.akive.api.mentoring.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.mentoring.domain.MentoringProfile;
import net.akive.api.user.infrastructure.entity.UserEntity;

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

    @Column(length = 140)
    private String goal;

    @Column(length = 100)
    private String preference;

    @Column(name = "`constraint`", length = 100)
    private String constraint;

    @Column(length = 140)
    private String weakness;

    @Column(length = 80)
    private String strength;

    @Column(length = 80)
    private String recurringPattern;

    @Column(length = 120)
    private String decisionOrCommitment;

    @Column(length = 220)
    private String studyPlan;

    @Column(length = 80)
    private String behavioralPattern;

    @Column(length = 140)
    private String effectiveAdvice;

    public static MentoringProfileEntity from(MentoringProfile mentoringProfile) {
        MentoringProfileEntity mentoringProfileEntity = new MentoringProfileEntity();
        mentoringProfileEntity.mentoringProfileId = mentoringProfile.getMentoringProfileId();
        mentoringProfileEntity.user = UserEntity.reference(mentoringProfile.getUserId());
        mentoringProfileEntity.goal = mentoringProfile.getGoal();
        mentoringProfileEntity.preference = mentoringProfile.getPreference();
        mentoringProfileEntity.constraint = mentoringProfile.getConstraint();
        mentoringProfileEntity.weakness = mentoringProfile.getWeakness();
        mentoringProfileEntity.strength = mentoringProfile.getStrength();
        mentoringProfileEntity.recurringPattern = mentoringProfile.getRecurringPattern();
        mentoringProfileEntity.decisionOrCommitment = mentoringProfile.getDecisionOrCommitment();
        mentoringProfileEntity.studyPlan = mentoringProfile.getStudyPlan();
        mentoringProfileEntity.behavioralPattern = mentoringProfile.getBehavioralPattern();
        mentoringProfileEntity.effectiveAdvice = mentoringProfile.getEffectiveAdvice();

        return mentoringProfileEntity;
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
