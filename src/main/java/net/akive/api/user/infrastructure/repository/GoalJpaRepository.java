package net.akive.api.user.infrastructure.repository;

import net.akive.api.user.infrastructure.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalJpaRepository extends JpaRepository<GoalEntity, Long> {
}
