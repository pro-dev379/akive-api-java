package net.akive.api.coaching.application.required;

import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;

import java.util.Optional;

public interface CoachingConversationRepository {

    CoachingConversation save(CoachingConversation coachingConversation);

    Optional<CoachingConversation> findByCoachingConversationIdAndUserId(
            Long coachingConversationId,
            Long userId
    );

    CoachingMessage saveMessage(CoachingMessage coachingMessage);

    CursorResult<CoachingConversationSummaryData, Long> findSummaryPageByUserIdAndContext(
            Long userId,
            ConversationContext context,
            CursorCondition<Long> cursorCondition
    );

    Optional<CoachingMessage> findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId
    );

    Optional<CoachingMessage> findLatestMessage(Long coachingConversationId);
}
