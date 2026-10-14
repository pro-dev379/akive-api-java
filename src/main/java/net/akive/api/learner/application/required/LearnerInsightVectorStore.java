package net.akive.api.learner.application.required;

import net.akive.api.learner.application.required.data.LearnerInsightVectorData;
import net.akive.api.learner.domain.InsightStatus;

import java.util.Collection;
import java.util.List;

public interface LearnerInsightVectorStore {

    void save(LearnerInsightVectorData learnerInsight, List<Float> embeddingValues);

    List<Long> findSimilarIds(
            Long userId,
            Collection<InsightStatus> statuses,
            List<Float> queryEmbeddingValues,
            int limit
    );
}
