package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalJpaRepository extends JpaRepository<GoalEntity, Long> {
}
