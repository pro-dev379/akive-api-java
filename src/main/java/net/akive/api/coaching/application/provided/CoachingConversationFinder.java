package net.akive.api.coaching.application.provided;

import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageResult;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;

public interface CoachingConversationFinder {

    CursorResult<CoachingConversationSummaryResult, Long> findSummaryPage(
            Long userId,
            ConversationContext context,
            CursorCondition<Long> cursorCondition
    );

    CursorResult<CoachingMessageResult, Long> findMessagePage(
            Long userId,
            Long coachingConversationId,
            CursorCondition<Long> cursorCondition
    );
}
