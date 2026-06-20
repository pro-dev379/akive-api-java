package net.akive.api.user.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.user.application.port.out.GoalRepository;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.infrastructure.entity.GoalEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class GoalJpaRepositoryAdapter implements GoalRepository {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        return goalJpaRepository.save(GoalEntity.fromModel(goal)).toModel();
    }
}
