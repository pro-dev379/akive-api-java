package net.akive.api.user.application.required;

import net.akive.api.user.domain.Goal;

import java.util.Optional;

public interface GoalRepository {

    Goal save(Goal goal);

    Optional<Goal> findByGoalId(Long goalId);

    Optional<Goal> findLatestByUserIdAndUserNotWithdrawn(Long userId);
}
