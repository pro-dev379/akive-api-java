package net.akive.api.coaching.application.required;

import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;

import java.util.Optional;

public interface CoachingConversationRepository {

    CoachingConversation save(CoachingConversation coachingConversation);

    Optional<CoachingConversation> findByCoachingConversationIdAndUserId(
            Long coachingConversationId,
            Long userId
    );

    CoachingMessage saveMessage(CoachingMessage coachingMessage);

    Optional<CoachingMessage> findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId
    );

    Optional<CoachingMessage> findLatestMessage(Long coachingConversationId);
}
