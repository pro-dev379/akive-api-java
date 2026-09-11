package net.akive.api.user.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.domain.Goal;

public interface GoalFinder {

    Goal findLatestByUserIdAndUserNotWithdrawn(Long userId) throws ResourceNotFoundException;
}
