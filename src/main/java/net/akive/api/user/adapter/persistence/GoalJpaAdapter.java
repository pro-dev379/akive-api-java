package net.akive.api.user.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import net.akive.api.user.application.required.GoalRepository;
import net.akive.api.user.domain.Goal;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class GoalJpaAdapter implements GoalRepository {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        return goalJpaRepository.save(GoalEntity.from(goal)).to();
    }
}
