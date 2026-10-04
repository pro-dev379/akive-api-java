package net.akive.api.user.application.required;

import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    Goal save(Goal goal);

    Optional<Goal> findByGoalId(Long goalId);

    Optional<Goal> findLatestByUserIdAndUserNotWithdrawn(Long userId);

    Optional<Goal> findLatestByUserIdAndCreatedAtBefore(Long userId, LocalDate date);

    List<GoalVersion> findVersionsForPeriod(Long userId, LocalDate startOn, LocalDate endOn);
}
