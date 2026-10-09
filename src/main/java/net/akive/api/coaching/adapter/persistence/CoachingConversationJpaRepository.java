package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface CoachingConversationJpaRepository extends JpaRepository<CoachingConversationEntity, Long> {
}
