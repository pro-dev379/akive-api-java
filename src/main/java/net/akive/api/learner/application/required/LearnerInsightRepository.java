package net.akive.api.learner.application.required;

import net.akive.api.learner.domain.EvidenceSourceType;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;

import java.util.Collection;
import java.util.List;

public interface LearnerInsightRepository {

    LearnerInsight save(LearnerInsight learnerInsight);

    LearnerInsightEvidence saveEvidence(LearnerInsightEvidence evidence);

    List<LearnerInsight> findAllByUserIdAndStatusIn(Long userId, Collection<InsightStatus> statuses);

    List<LearnerInsight> findAllByUserIdAndLearnerInsightIdInAndStatusIn(
            Long userId,
            Collection<Long> learnerInsightIds,
            Collection<InsightStatus> statuses
    );

    boolean existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
            Long learnerInsightId,
            EvidenceSourceType sourceType,
            Long sourceId
    );
}
