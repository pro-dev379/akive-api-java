package net.akive.api.mentoring.domain;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class MentoringPolicy {

    public static final int GOAL_MAX_LENGTH = 140;
    public static final int PREFERENCE_MAX_LENGTH = 100;
    public static final int CONSTRAINT_MAX_LENGTH = 100;
    public static final int WEAKNESS_MAX_LENGTH = 140;
    public static final int STRENGTH_MAX_LENGTH = 80;
    public static final int RECURRING_PATTERN_MAX_LENGTH = 80;
    public static final int DECISION_OR_COMMITMENT_MAX_LENGTH = 120;
    public static final int STUDY_PLAN_MAX_LENGTH = 220;
    public static final int BEHAVIORAL_PATTERN_MAX_LENGTH = 80;
    public static final int EFFECTIVE_ADVICE_MAX_LENGTH = 140;
}
