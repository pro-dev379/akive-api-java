package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.provided.CoachingConversationFinder;
import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class CoachingConversationQueryService implements CoachingConversationFinder {

    private final CoachingConversationRepository coachingConversationRepository;

    @Override
    public CursorResult<CoachingConversationSummaryResult, Long> findSummaryPage(
            Long userId,
            ConversationContext context,
            CursorCondition<Long> cursorCondition
    ) {
        validateLatestMessageIdCursor(cursorCondition.cursor());

        CursorResult<CoachingConversationSummaryData, Long> summaryPage =
                coachingConversationRepository.findSummaryPageByUserIdAndContext(
                        userId,
                        context,
                        cursorCondition
                );

        return new CursorResult<>(
                summaryPage.cursorInfo(),
                summaryPage.contents().stream()
                        .map(CoachingConversationSummaryResult::from)
                        .toList()
        );
    }

    private void validateLatestMessageIdCursor(Long latestMessageIdCursor) {
        if (latestMessageIdCursor != null && latestMessageIdCursor <= 0) {
            throw new IllegalArgumentException("Cursor must be positive.");
        }
    }
}
