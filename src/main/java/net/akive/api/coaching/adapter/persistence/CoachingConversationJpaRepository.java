package net.akive.api.coaching.adapter.persistence;

import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.ConversationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface CoachingConversationJpaRepository extends JpaRepository<CoachingConversationEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<CoachingConversationEntity> findByCoachingConversationIdAndUser_UserId(
            Long coachingConversationId,
            Long userId
    );

    @Query("""
            select new net.akive.api.coaching.application.required.data.CoachingConversationSummaryData(
                conversation.coachingConversationId,
                conversation.title,
                conversation.context,
                message.content,
                message.createdAt,
                conversation.createdAt,
                conversation.updatedAt,
                message.coachingMessageId
            )
            from CoachingConversationEntity conversation
            join CoachingMessageEntity message
              on message.coachingConversation = conversation
            where conversation.user.userId = :userId
              and conversation.context = :context
              and message.coachingMessageId = (
                  select max(latestMessage.coachingMessageId)
                  from CoachingMessageEntity latestMessage
                  where latestMessage.coachingConversation = conversation
              )
              and (
                  :latestMessageIdCursor is null
                  or message.coachingMessageId < :latestMessageIdCursor
              )
            order by message.coachingMessageId desc
            """)
    List<CoachingConversationSummaryData> findSummariesByUserIdAndContextAndLatestMessageIdBefore(
            Long userId,
            ConversationContext context,
            Long latestMessageIdCursor,
            Pageable pageable
    );
}
