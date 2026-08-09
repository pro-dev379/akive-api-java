package net.akive.api.mentoring.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.mentoring.domain.MentoringProfileDetail;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class MentoringProfileDetailEntity {

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

    public static MentoringProfileDetailEntity from(MentoringProfileDetail detail) {
        if (detail == null) return null;

        return new MentoringProfileDetailEntity(
                detail.goal(),
                detail.preference(),
                detail.constraint(),
                detail.weakness(),
                detail.strength(),
                detail.recurringPattern(),
                detail.decisionOrCommitment(),
                detail.studyPlan(),
                detail.behavioralPattern(),
                detail.effectiveAdvice()
        );
    }

    public MentoringProfileDetail to() {
        return new MentoringProfileDetail(
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
