package net.akive.api.coaching.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.adapter.persistence.entity.CoachingConversationEntity;
import net.akive.api.coaching.adapter.persistence.entity.CoachingMessageEntity;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<CoachingConversation> findByCoachingConversationIdAndUserId(
            Long coachingConversationId,
            Long userId
    ) {
        return coachingConversationJpaRepository
                .findByCoachingConversationIdAndUser_UserId(coachingConversationId, userId)
                .map(CoachingConversationEntity::to);
    }

    @Override
    public CoachingMessage saveMessage(CoachingMessage coachingMessage) {
        return coachingMessageJpaRepository.save(CoachingMessageEntity.from(coachingMessage)).to();
    }

    @Override
    public Optional<CoachingMessage> findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId
    ) {
        return coachingMessageJpaRepository
                .findByIdAndConversationIdAndUserId(coachingMessageId, coachingConversationId, userId)
                .map(CoachingMessageEntity::to);
    }

    @Override
    public Optional<CoachingMessage> findLatestMessage(Long coachingConversationId) {
        return coachingMessageJpaRepository
                .findLatestByConversationId(coachingConversationId)
                .map(CoachingMessageEntity::to);
    }
}
