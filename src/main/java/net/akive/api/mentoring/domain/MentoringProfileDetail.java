package net.akive.api.mentoring.domain;

import static net.akive.api.common.domain.DomainValidator.requireMaxLengthIfPresent;
import static net.akive.api.mentoring.domain.MentoringPolicy.*;

public record MentoringProfileDetail(
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
    
    public MentoringProfileDetail {
        requireMaxLengthIfPresent(goal, GOAL_MAX_LENGTH);
        requireMaxLengthIfPresent(preference, PREFERENCE_MAX_LENGTH);
        requireMaxLengthIfPresent(constraint, CONSTRAINT_MAX_LENGTH);
        requireMaxLengthIfPresent(weakness, WEAKNESS_MAX_LENGTH);
        requireMaxLengthIfPresent(strength, STRENGTH_MAX_LENGTH);
        requireMaxLengthIfPresent(recurringPattern, RECURRING_PATTERN_MAX_LENGTH);
        requireMaxLengthIfPresent(decisionOrCommitment, DECISION_OR_COMMITMENT_MAX_LENGTH);
        requireMaxLengthIfPresent(studyPlan, STUDY_PLAN_MAX_LENGTH);
        requireMaxLengthIfPresent(behavioralPattern, BEHAVIORAL_PATTERN_MAX_LENGTH);
        requireMaxLengthIfPresent(effectiveAdvice, EFFECTIVE_ADVICE_MAX_LENGTH);
    }
}
