package net.akive.api.learner.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.learner.adapter.persistence.entity.LearnerInsightEntity;
import net.akive.api.learner.adapter.persistence.entity.LearnerInsightEvidenceEntity;
import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.EvidenceSourceType;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
class LearnerInsightJpaAdapter implements LearnerInsightRepository {

    private final LearnerInsightJpaRepository learnerInsightJpaRepository;
    private final LearnerInsightEvidenceJpaRepository learnerInsightEvidenceJpaRepository;

    @Override
    public LearnerInsight save(LearnerInsight learnerInsight) {
        return learnerInsightJpaRepository.save(LearnerInsightEntity.from(learnerInsight)).to();
    }

    @Override
    public LearnerInsightEvidence saveEvidence(LearnerInsightEvidence evidence) {
        return learnerInsightEvidenceJpaRepository
                .save(LearnerInsightEvidenceEntity.from(evidence))
                .to();
    }

    @Override
    public List<LearnerInsight> findAllByUserIdAndStatusIn(Long userId, Collection<InsightStatus> statuses) {
        return learnerInsightJpaRepository.findAllByUser_UserIdAndStatusIn(userId, statuses)
                .stream()
                .map(LearnerInsightEntity::to)
                .toList();
    }

    @Override
    public List<LearnerInsight> findAllByUserIdAndLearnerInsightIdInAndStatusIn(
            Long userId,
            Collection<Long> learnerInsightIds,
            Collection<InsightStatus> statuses
    ) {
        return learnerInsightJpaRepository
                .findAllByUser_UserIdAndLearnerInsightIdInAndStatusIn(userId, learnerInsightIds, statuses)
                .stream()
                .map(LearnerInsightEntity::to)
                .toList();
    }

    @Override
    public boolean existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
            Long learnerInsightId,
            EvidenceSourceType sourceType,
            Long sourceId
    ) {
        return learnerInsightEvidenceJpaRepository
                .existsByLearnerInsight_LearnerInsightIdAndSourceTypeAndSourceId(
                        learnerInsightId,
                        sourceType,
                        sourceId
                );
    }
}
