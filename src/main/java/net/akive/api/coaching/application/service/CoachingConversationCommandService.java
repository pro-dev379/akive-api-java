package net.akive.api.coaching.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.application.required.CoachingConversationRepository;
import net.akive.api.coaching.application.required.CoachingLlm;
import net.akive.api.coaching.domain.CoachingConversation;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CoachingConversationCommandService implements CoachingConversationCreator {

    private final UserFinder userFinder;
    private final CoachingLlm coachingLlm;
    private final CoachingConversationRepository coachingConversationRepository;

    @Override
    public Long create(CoachingConversationCreateCommand command) throws ResourceNotFoundException {
        User user = userFinder.findOrThrow(command.userId());
        String llmConversationId = coachingLlm.createConversation();

        return coachingConversationRepository
                .save(CoachingConversation.create(user, llmConversationId, command.context()))
                .getCoachingConversationId();
    }
}
