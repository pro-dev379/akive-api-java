package net.akive.api.learner.adapter.persistence;

import net.akive.api.learner.adapter.persistence.entity.LearnerInsightEntity;
import net.akive.api.learner.domain.InsightStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface LearnerInsightJpaRepository extends JpaRepository<LearnerInsightEntity, Long> {

    @EntityGraph(attributePaths = "user")
    List<LearnerInsightEntity> findAllByUser_UserIdAndStatusIn(Long userId, Collection<InsightStatus> statuses);

    @EntityGraph(attributePaths = "user")
    List<LearnerInsightEntity> findAllByUser_UserIdAndLearnerInsightIdInAndStatusIn(
            Long userId,
            Collection<Long> learnerInsightIds,
            Collection<InsightStatus> statuses
    );
}
