package net.akive.api.coaching.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import net.akive.api.coaching.adapter.persistence.entity.CoachingMessageEntity;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorCondition;
import net.akive.api.common.pagination.CursorResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class CoachingConversationJpaAdapter implements CoachingConversationRepository {

    private final CoachingConversationJpaRepository coachingConversationJpaRepository;
    private final CoachingMessageJpaRepository coachingMessageJpaRepository;

    @Override
    @Transactional
    public CoachingConversation save(CoachingConversation coachingConversation) {
        return coachingConversationJpaRepository.save(CoachingConversationEntity.from(coachingConversation)).to();
    }

    @Override
    public CoachingMessage saveCoachingMessage(CoachingMessage coachingMessage) {
        return coachingMessageJpaRepository.save(CoachingMessageEntity.from(coachingMessage)).to();
    }

    @Override
    public Optional<CoachingConversation> findByCoachingConversationIdAndUserId(
            Long coachingConversationId,
            Long userId
    ) {
        return coachingConversationJpaRepository
                .findByCoachingConversationIdAndUser_UserId(coachingConversationId, userId)
                .map(CoachingConversationEntity::to);
    }

    @Override
    public CursorResult<CoachingConversationSummaryData, Long> findSummaryPageByUserIdAndContext(
            Long userId,
            ConversationContext context,
            CursorCondition<Long> cursorCondition
    ) {
        List<CoachingConversationSummaryData> fetchedSummaries = coachingConversationJpaRepository
                .findSummariesByUserIdAndContextAndLatestCoachingMessageIdBefore(
                        userId,
                        context,
                        cursorCondition.cursor(),
                        PageRequest.of(0, cursorCondition.size() + 1)
                );

        boolean hasNext = fetchedSummaries.size() > cursorCondition.size();
        List<CoachingConversationSummaryData> summaries = hasNext
                ? fetchedSummaries.subList(0, cursorCondition.size())
                : fetchedSummaries;
        Long nextCursor = hasNext
                ? summaries.getLast().latestMessageId()
                : null;

        return CursorResult.of(nextCursor, hasNext, summaries);
    }

    @Override
    public Optional<CoachingMessage> findCoachingMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId
    ) {
        return coachingMessageJpaRepository
                .findByIdAndCoachingConversationIdAndUserId(coachingMessageId, coachingConversationId, userId)
                .map(CoachingMessageEntity::to);
    }

    @Override
    public Optional<CoachingMessage> findLatestCoachingMessage(Long coachingConversationId) {
        return coachingMessageJpaRepository
                .findLatestByCoachingConversationId(coachingConversationId)
                .map(CoachingMessageEntity::to);
    }

    @Override
    public CursorResult<CoachingMessage, Long> findCoachingMessagePageByCoachingConversationId(
            Long coachingConversationId,
            CursorCondition<Long> cursorCondition
    ) {
        List<CoachingMessageEntity> fetchedMessages = coachingMessageJpaRepository
                .findPageByCoachingConversationIdAndCoachingMessageIdBefore(
                        coachingConversationId,
                        cursorCondition.cursor(),
                        PageRequest.of(0, cursorCondition.size() + 1)
                );

        boolean hasNext = fetchedMessages.size() > cursorCondition.size();
        List<CoachingMessage> messages = fetchedMessages.stream()
                .limit(cursorCondition.size())
                .map(CoachingMessageEntity::to)
                .toList();
        Long nextCursor = hasNext
                ? messages.getLast().getCoachingMessageId()
                : null;

        return CursorResult.of(nextCursor, hasNext, messages.reversed());
    }

    @Override
    public void delete(CoachingConversation coachingConversation) {
        coachingConversationJpaRepository.delete(CoachingConversationEntity.from(coachingConversation));
    }
}
