package net.akive.api.user.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.dto.GoalCreateCommand;
import net.akive.api.user.domain.Goal;

public interface GoalCreator {

    Goal create(GoalCreateCommand command) throws ResourceNotFoundException;
}
