package net.akive.api.learner.adapter.persistence;

import net.akive.api.learner.adapter.persistence.entity.LearnerInsightEvidenceEntity;
import net.akive.api.learner.domain.EvidenceSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

interface LearnerInsightEvidenceJpaRepository extends JpaRepository<LearnerInsightEvidenceEntity, Long> {

    boolean existsByLearnerInsight_LearnerInsightIdAndSourceTypeAndSourceId(
            Long learnerInsightId,
            EvidenceSourceType sourceType,
            Long sourceId
    );
}
