package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.provided.CoachingConversationFinder;
import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.application.provided.dto.CoachingMessageResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.COACHING_CONVERSATION_NOT_FOUND;

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
        validateCoachingMessageIdCursor(cursorCondition.cursor());

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

    @Override
    public CursorResult<CoachingMessageResult, Long> findMessagePage(
            Long userId,
            Long coachingConversationId,
            CursorCondition<Long> cursorCondition
    ) {
        validateCoachingMessageIdCursor(cursorCondition.cursor());
        validateConversationExists(coachingConversationId, userId);

        CursorResult<CoachingMessage, Long> messagePage =
                coachingConversationRepository.findCoachingMessagePageByCoachingConversationId(
                        coachingConversationId,
                        cursorCondition
                );

        return new CursorResult<>(
                messagePage.cursorInfo(),
                messagePage.contents().stream()
                        .map(CoachingMessageResult::from)
                        .toList()
        );
    }

    private void validateCoachingMessageIdCursor(Long coachingMessageIdCursor) {
        if (coachingMessageIdCursor != null && coachingMessageIdCursor <= 0) {
            throw new IllegalArgumentException("Cursor must be positive.");
        }
    }

    private void validateConversationExists(Long coachingConversationId, Long userId) {
        coachingConversationRepository.findByCoachingConversationIdAndUserId(coachingConversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(COACHING_CONVERSATION_NOT_FOUND));
    }
}
