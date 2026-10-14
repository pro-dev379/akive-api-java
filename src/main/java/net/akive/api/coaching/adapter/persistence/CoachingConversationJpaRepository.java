package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CoachingConversationJpaRepository extends JpaRepository<CoachingConversationEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<CoachingConversationEntity> findByCoachingConversationIdAndUser_UserId(
            Long coachingConversationId,
            Long userId
    );
}
