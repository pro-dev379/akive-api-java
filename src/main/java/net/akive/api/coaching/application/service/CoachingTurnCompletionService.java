package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.event.CoachingTurnCompletedEvent;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.eventprocessing.application.provided.IntegrationEventRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;

@Service
@Transactional
@RequiredArgsConstructor
class CoachingTurnCompletionService {

    private final CoachingConversationRepository coachingConversationRepository;
    private final IntegrationEventRecorder integrationEventRecorder;

    void complete(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData
    ) {
        CoachingMessage assistantMessage = findOrSaveAssistantMessage(coachingConversation, recoveryData);
        CoachingTurnCompletedEvent completedEvent = createCompletedEvent(
                coachingConversation,
                recoveryData,
                assistantMessage
        );

        integrationEventRecorder.record(completedEvent);
    }

    private CoachingMessage findOrSaveAssistantMessage(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData
    ) {
        return coachingConversationRepository
                .findLatestCoachingMessage(coachingConversation.getCoachingConversationId())
                .filter(message -> message.getRole() == ASSISTANT)
                .filter(message -> recoveryData.content().equals(message.getContent()))
                .filter(message -> recoveryData.completionStatus().equals(message.getCompletionStatus()))
                .orElseGet(() -> coachingConversationRepository.saveCoachingMessage(
                        CoachingMessage.fromAssistant(
                                coachingConversation,
                                recoveryData.content(),
                                recoveryData.completionStatus()
                        )
                ));
    }

    private CoachingTurnCompletedEvent createCompletedEvent(
            CoachingConversation coachingConversation,
            CoachingTurnRecoveryData recoveryData,
            CoachingMessage assistantMessage
    ) {
        return new CoachingTurnCompletedEvent(
                recoveryData.eventId(),
                coachingConversation.getUser().getUserId(),
                coachingConversation.getCoachingConversationId(),
                recoveryData.userMessageId(),
                assistantMessage.getCoachingMessageId(),
                assistantMessage.getCreatedAt()
        );
    }
}
