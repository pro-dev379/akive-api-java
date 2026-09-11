package net.akive.api.user.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class GoalJpaAdapter implements GoalRepository {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        return goalJpaRepository.save(GoalEntity.from(goal)).to();
    }

    @Override
    public Optional<Goal> findByGoalId(Long goalId) {
        return goalJpaRepository.findById(goalId).map(GoalEntity::to);
    }

    @Override
    public Optional<Goal> findLatestByUserIdAndUserNotWithdrawn(Long userId) {
        return goalJpaRepository
                .findFirstByUser_UserIdAndUser_Withdrawal_WithdrawnAtIsNullOrderByGoalIdDesc(userId)
                .map(GoalEntity::to);
    }
}
