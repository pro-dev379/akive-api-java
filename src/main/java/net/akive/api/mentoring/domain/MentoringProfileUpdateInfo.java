package net.akive.api.mentoring.domain;

public record MentoringProfileUpdateInfo(
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
}
