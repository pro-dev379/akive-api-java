package net.akive.api.coaching.application.provided;

import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendResult;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;

public interface CoachingMessageSender {

    CoachingMessageSendResult send(CoachingMessageSendCommand command)
            throws ResourceNotFoundException, ResourceConflictException;
}
