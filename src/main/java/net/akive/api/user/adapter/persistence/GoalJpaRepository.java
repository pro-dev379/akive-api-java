package net.akive.api.user.adapter.persistence;

import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalJpaRepository extends JpaRepository<GoalEntity, Long> {

    Optional<GoalEntity> findFirstByUser_UserIdAndUser_Withdrawal_WithdrawnAtIsNullOrderByGoalIdDesc(Long userId);
}
