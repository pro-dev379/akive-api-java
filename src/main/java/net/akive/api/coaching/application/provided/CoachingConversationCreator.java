package net.akive.api.coaching.application.provided;

import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

public interface CoachingConversationCreator {

    Long create(CoachingConversationCreateCommand command) throws ResourceNotFoundException;

    void delete(Long coachingConversationId, Long userId)
            throws ResourceNotFoundException, ResourceConflictException;
}
