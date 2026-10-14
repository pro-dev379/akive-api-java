package net.akive.api.learner.domain;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class LearnerPolicy {

    public static final int INSIGHT_CONTENT_MAX_LENGTH = 500;
    public static final int EVIDENCE_CONTENT_MAX_LENGTH = 500;
    public static final long INSIGHT_ANALYSIS_OUTPUT_TOKEN_LIMIT = 4096L;
    public static final int ANALYSIS_TARGET_MAX_COUNT = 20;
    public static final int COACHING_CONTEXT_MAX_COUNT = 10;
}
