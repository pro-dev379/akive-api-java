package net.akive.api.mentoring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.mentoring.domain.MentoringPolicy.*;

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
                .goal(requireMaxLengthIfPresent(goal, GOAL_MAX_LENGTH))
                .preference(requireMaxLengthIfPresent(preference, PREFERENCE_MAX_LENGTH))
                .constraint(requireMaxLengthIfPresent(constraint, CONSTRAINT_MAX_LENGTH))
                .weakness(requireMaxLengthIfPresent(weakness, WEAKNESS_MAX_LENGTH))
                .strength(requireMaxLengthIfPresent(strength, STRENGTH_MAX_LENGTH))
                .recurringPattern(requireMaxLengthIfPresent(recurringPattern, RECURRING_PATTERN_MAX_LENGTH))
                .decisionOrCommitment(requireMaxLengthIfPresent(decisionOrCommitment, DECISION_OR_COMMITMENT_MAX_LENGTH))
                .studyPlan(requireMaxLengthIfPresent(studyPlan, STUDY_PLAN_MAX_LENGTH))
                .behavioralPattern(requireMaxLengthIfPresent(behavioralPattern, BEHAVIORAL_PATTERN_MAX_LENGTH))
                .effectiveAdvice(requireMaxLengthIfPresent(effectiveAdvice, EFFECTIVE_ADVICE_MAX_LENGTH))
                .build();
    }

    public static MentoringProfile createDefault(Long userId) {
        return MentoringProfile.builder()
                .userId(requirePositive(userId))
                .build();
    }
}
