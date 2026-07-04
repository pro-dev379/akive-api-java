package net.akive.api.mentoring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class MentoringProfile {

    private Long mentoringProfileId;
    private Long userId;
    private String goal;
    private String preference;
    private String constraint;
    private String weakness;
    private String strength;
    private String recurringPattern;
    private String decisionOrCommitment;
    private String studyPlan;
    private String behavioralPattern;
    private String effectiveAdvice;

    public static MentoringProfile create(
            Long mentoringProfileId,
            Long userId,
            String goal,
            String preference,
            String constraint,
            String weakness,
            String strength,
            String recurringPattern,
            String decisionOrCommitment,
            String studyPlan,
            String behavioralPattern,
            String effectiveAdvice
    ) {
        return MentoringProfile.builder()
                .mentoringProfileId(mentoringProfileId)
                .userId(userId)
                .goal(goal)
                .preference(preference)
                .constraint(constraint)
                .weakness(weakness)
                .strength(strength)
                .recurringPattern(recurringPattern)
                .decisionOrCommitment(decisionOrCommitment)
                .studyPlan(studyPlan)
                .behavioralPattern(behavioralPattern)
                .effectiveAdvice(effectiveAdvice)
                .build();
    }

    public static MentoringProfile createDefault(Long userId) {
        return MentoringProfile.builder()
                .userId(userId)
                .build();
    }
}
