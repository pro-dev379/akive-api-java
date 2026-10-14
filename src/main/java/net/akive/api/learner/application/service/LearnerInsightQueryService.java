package net.akive.api.learner.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.learner.application.provided.LearnerInsightFinder;
import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.application.required.LearnerInsightVectorStore;
import net.akive.api.learner.application.required.TextEmbeddingModel;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightSearch;

@Slf4j
@Service
@RequiredArgsConstructor
class LearnerInsightQueryService implements LearnerInsightFinder {

    private final TextEmbeddingModel textEmbeddingModel;
    private final LearnerInsightVectorStore learnerInsightVectorStore;
    private final LearnerInsightRepository learnerInsightRepository;

    @Override
    public List<LearnerInsight> findRelevantInsights(
            Long userId,
            String query,
            Collection<InsightStatus> statuses,
            int limit
    ) {
        try {
            List<Float> queryEmbeddingValues = textEmbeddingModel.embed(forLearnerInsightSearch(userId, query));

            List<Long> learnerInsightIds = learnerInsightVectorStore.findSimilarIds(
                    userId,
                    statuses,
                    queryEmbeddingValues,
                    limit
            );

            if (learnerInsightIds.isEmpty()) {
                return findFallbackInsights(userId, statuses, limit);
            }

            Map<Long, LearnerInsight> learnerInsightById = learnerInsightRepository
                    .findAllByUserIdAndLearnerInsightIdInAndStatusIn(userId, learnerInsightIds, statuses)
                    .stream()
                    .collect(Collectors.toMap(LearnerInsight::getLearnerInsightId, Function.identity()));

            return learnerInsightIds.stream()
                    .map(learnerInsightById::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("Failed to find relevant learner insights. Falling back to the database: userId={}", userId, e);

            return findFallbackInsights(userId, statuses, limit);
        }
    }

    private List<LearnerInsight> findFallbackInsights(
            Long userId,
            Collection<InsightStatus> statuses,
            int limit
    ) {
        return learnerInsightRepository.findAllByUserIdAndStatusIn(userId, statuses)
                .stream()
                .limit(limit)
                .toList();
    }
}
