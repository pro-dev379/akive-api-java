package net.akive.api.coaching.application.prompt.fixture;

import net.akive.api.coaching.application.prompt.CoachingPromptComposition;

public class CoachingPromptCompositionFixture {

    public static final String MESSAGE = "학습 계획을 세우고 싶어요.";
    public static final String CONTEXT_SECTION_NAME = "learnerInsights";
    public static final String CONTEXT_SECTION_CONTENT = "계획을 작게 나누면 꾸준히 실천하는 경향이 있습니다.";

    private CoachingPromptCompositionFixture() {
    }

    public static CoachingPromptComposition.ContextSection createContextSection() {
        return new CoachingPromptComposition.ContextSection(CONTEXT_SECTION_NAME, CONTEXT_SECTION_CONTENT);
    }
}
