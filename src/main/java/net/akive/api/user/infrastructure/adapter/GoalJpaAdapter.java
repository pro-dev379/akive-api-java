package net.akive.api.user.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.out.SaveGoalPort;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.infrastructure.repository.GoalJpaRepository;
import net.akive.api.user.infrastructure.entity.GoalEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class GoalJpaAdapter implements SaveGoalPort {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        return goalJpaRepository.save(GoalEntity.from(goal)).to();
    }
}
