package net.akive.api.learner.adapter.coaching;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.prompt.CoachingPromptComposition;
import net.akive.api.coaching.application.required.CoachingContextProvider;
import net.akive.api.learner.application.provided.LearnerInsightFinder;
import net.akive.api.learner.domain.LearnerInsight;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.LearnerPolicy.COACHING_CONTEXT_MAX_COUNT;

@Component
@RequiredArgsConstructor
class LearnerInsightCoachingContextAdapter implements CoachingContextProvider {

    private final LearnerInsightFinder learnerInsightFinder;

    @Override
    public List<CoachingPromptComposition.ContextSection> findContextSections(Long userId, String message) {
        List<LearnerInsight> relevantInsights = learnerInsightFinder.findRelevantInsights(
                userId,
                message,
                Set.of(ACTIVE),
                COACHING_CONTEXT_MAX_COUNT
        );

        if (relevantInsights.isEmpty()) {
            return List.of();
        }

        String content = relevantInsights.stream()
                .map(this::formatInsight)
                .collect(joining("\n\n"));

        return List.of(new CoachingPromptComposition.ContextSection("learnerInsights", content));
    }

    private String formatInsight(LearnerInsight insight) {
        return """
                <insight type="%s" confidence="%s">
                %s
                </insight>
                """.formatted(
                insight.getInsightType(),
                insight.getConfidence(),
                insight.getContent()
        ).strip();
    }
}
