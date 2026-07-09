package net.akive.api.mentoring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;

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
                .userId(requirePositive(userId))
                .goal(requireMaxLengthIfPresent(goal, 140))
                .preference(requireMaxLengthIfPresent(preference, 100))
                .constraint(requireMaxLengthIfPresent(constraint, 100))
                .weakness(requireMaxLengthIfPresent(weakness, 140))
                .strength(requireMaxLengthIfPresent(strength, 80))
                .recurringPattern(requireMaxLengthIfPresent(recurringPattern, 80))
                .decisionOrCommitment(requireMaxLengthIfPresent(decisionOrCommitment, 120))
                .studyPlan(requireMaxLengthIfPresent(studyPlan, 220))
                .behavioralPattern(requireMaxLengthIfPresent(behavioralPattern, 80))
                .effectiveAdvice(requireMaxLengthIfPresent(effectiveAdvice, 140))
                .build();
    }

    public static MentoringProfile createDefault(Long userId) {
        return MentoringProfile.builder()
                .userId(requirePositive(userId))
                .build();
    }
}
