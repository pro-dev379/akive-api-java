package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.provided.CoachingTurnFinder;
import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;
import net.akive.api.coaching.application.provided.dto.CoachingTurnResult;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.MessageRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.akive.api.coaching.domain.MessageRole.ASSISTANT;
import static net.akive.api.coaching.domain.MessageRole.USER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CoachingTurnQueryService implements CoachingTurnFinder {

    private final CoachingConversationRepository coachingConversationRepository;

    @Override
    public Optional<CoachingTurnResult> find(CoachingTurnFindQuery query) {
        return findMessage(
                query.userMessageId(),
                query.coachingConversationId(),
                query.userId(),
                USER
            )
            .flatMap(userMessage -> findMessage(
                    query.assistantMessageId(),
                    query.coachingConversationId(),
                    query.userId(),
                    ASSISTANT
            ).map(assistantMessage -> new CoachingTurnResult(
                    userMessage.getContent(),
                    assistantMessage.getContent()
            )));
    }

    private Optional<CoachingMessage> findMessage(
            Long coachingMessageId,
            Long coachingConversationId,
            Long userId,
            MessageRole expectedRole
    ) {
        return coachingConversationRepository
                .findMessageByCoachingMessageIdAndCoachingConversationIdAndUserId(
                        coachingMessageId,
                        coachingConversationId,
                        userId
                )
                .filter(message -> message.getRole() == expectedRole);
    }
}
