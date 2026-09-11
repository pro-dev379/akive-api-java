package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.GOAL_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class GoalService implements GoalFinder {

    private final GoalRepository goalRepository;

    @Override
    public Goal findLatestByUserIdAndUserNotWithdrawn(Long userId) {
        return goalRepository.findLatestByUserIdAndUserNotWithdrawn(userId).
                orElseThrow(() -> new ResourceNotFoundException(GOAL_NOT_FOUND));
    }
}
