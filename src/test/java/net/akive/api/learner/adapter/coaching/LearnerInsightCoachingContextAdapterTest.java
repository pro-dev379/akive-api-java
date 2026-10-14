package net.akive.api.learner.adapter.coaching;

import net.akive.api.coaching.application.prompt.CoachingPromptComposition;
import net.akive.api.learner.application.provided.LearnerInsightFinder;
import net.akive.api.learner.domain.LearnerInsight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static net.akive.api.coaching.application.prompt.fixture.CoachingPromptCompositionFixture.MESSAGE;
import static net.akive.api.learner.domain.InsightConfidence.HIGH;
import static net.akive.api.learner.domain.InsightConfidence.MEDIUM;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.InsightType.STRENGTH;
import static net.akive.api.learner.domain.LearnerPolicy.COACHING_CONTEXT_MAX_COUNT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LearnerInsightCoachingContextAdapterTest {

    @Mock
    LearnerInsightFinder learnerInsightFinder;

    @InjectMocks
    LearnerInsightCoachingContextAdapter learnerInsightCoachingContextAdapter;

    @Test
    void 학습자_인사이트를_코칭_컨텍스트로_구성한다() {
        LearnerInsight behaviorPatternInsight = reconstructLearnerInsight(
                LEARNER_INSIGHT_ID,
                createReconstructedUser(),
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                MEDIUM,
                ACTIVE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
        LearnerInsight strengthInsight = reconstructLearnerInsight(
                ANOTHER_LEARNER_INSIGHT_ID,
                createReconstructedUser(),
                STRENGTH,
                ANOTHER_INSIGHT_CONTENT,
                HIGH,
                ACTIVE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
        given(learnerInsightFinder.findRelevantInsights(
                USER_ID,
                MESSAGE,
                Set.of(ACTIVE),
                COACHING_CONTEXT_MAX_COUNT
        )).willReturn(List.of(behaviorPatternInsight, strengthInsight));

        List<CoachingPromptComposition.ContextSection> contextSections =
                learnerInsightCoachingContextAdapter.findContextSections(USER_ID, MESSAGE);

        assertThat(contextSections).containsExactly(new CoachingPromptComposition.ContextSection(
                "learnerInsights",
                """
                <insight type="%s" confidence="%s">
                %s
                </insight>

                <insight type="%s" confidence="%s">
                %s
                </insight>
                """.formatted(
                        BEHAVIOR_PATTERN,
                        MEDIUM,
                        INSIGHT_CONTENT,
                        STRENGTH,
                        HIGH,
                        ANOTHER_INSIGHT_CONTENT
                ).strip()
        ));
        then(learnerInsightFinder).should().findRelevantInsights(
                USER_ID,
                MESSAGE,
                Set.of(ACTIVE),
                COACHING_CONTEXT_MAX_COUNT
        );
    }

    @Test
    void 학습자_인사이트가_없으면_코칭_컨텍스트를_반환하지_않는다() {
        given(learnerInsightFinder.findRelevantInsights(
                USER_ID,
                MESSAGE,
                Set.of(ACTIVE),
                COACHING_CONTEXT_MAX_COUNT
        )).willReturn(List.of());

        List<CoachingPromptComposition.ContextSection> contextSections =
                learnerInsightCoachingContextAdapter.findContextSections(USER_ID, MESSAGE);

        assertThat(contextSections).isEmpty();
        then(learnerInsightFinder).should().findRelevantInsights(
                USER_ID,
                MESSAGE,
                Set.of(ACTIVE),
                COACHING_CONTEXT_MAX_COUNT
        );
    }
}
