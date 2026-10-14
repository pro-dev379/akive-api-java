package net.akive.api.learner.application.provided;

import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;

import java.util.Collection;
import java.util.List;

public interface LearnerInsightFinder {

    List<LearnerInsight> findRelevantInsights(
            Long userId,
            String query,
            Collection<InsightStatus> statuses,
            int limit
    );
}
