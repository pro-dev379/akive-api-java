package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.adapter.persistence.entity.CoachingMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface CoachingMessageJpaRepository extends JpaRepository<CoachingMessageEntity, Long> {

    @Query("""
            select message
            from CoachingMessageEntity message
            join fetch message.coachingConversation conversation
            join fetch conversation.user user
            where message.coachingMessageId = :coachingMessageId
              and conversation.coachingConversationId = :coachingConversationId
              and user.userId = :userId
    """)
    Optional<CoachingMessageEntity> findByIdAndCoachingConversationIdAndUserId(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId
    );

    @Query("""
            select message
            from CoachingMessageEntity message
            join fetch message.coachingConversation conversation
            join fetch conversation.user
            where conversation.coachingConversationId = :coachingConversationId
            order by message.coachingMessageId desc
            limit 1
    """)
    Optional<CoachingMessageEntity> findLatestByCoachingConversationId(Long coachingConversationId);

    @Query("""
            select message
            from CoachingMessageEntity message
            join fetch message.coachingConversation conversation
            join fetch conversation.user
            where conversation.coachingConversationId = :coachingConversationId
              and (
                  :coachingMessageIdCursor is null
                  or message.coachingMessageId < :coachingMessageIdCursor
              )
            order by message.coachingMessageId desc
    """)
    List<CoachingMessageEntity> findPageByCoachingConversationIdAndCoachingMessageIdBefore(
            Long coachingConversationId,
            Long coachingMessageIdCursor,
            Pageable pageable
    );
}
