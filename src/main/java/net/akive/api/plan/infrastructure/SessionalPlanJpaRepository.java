package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionalPlanJpaRepository extends JpaRepository<SessionalPlanEntity, Long> {

    List<SessionalPlanEntity> findByUser_userIdOrderBySessionalPlanIdDesc(Long userId);
}