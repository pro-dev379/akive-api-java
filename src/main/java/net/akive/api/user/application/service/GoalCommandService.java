package net.akive.api.user.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.user.application.provided.GoalCreator;
import net.akive.api.user.application.provided.dto.GoalCreateCommand;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.application.required.UserRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class GoalCommandService implements GoalCreator {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    @Override
    public Goal create(GoalCreateCommand command) throws ResourceNotFoundException {
        User user = findUserOrThrow(command.userId());

        Goal goal = Goal.create(
                user,
                command.attendOn(),
                command.rest10MinSec(),
                command.mealNum(),
                command.meal1TimeSec()
        );

        return goalRepository.save(goal);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findByUserIdAndNotWithdrawn(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }
}
