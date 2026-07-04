package net.akive.api.plan.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionalPlanPortJpaAdapter implements LoadSessionalPlanPort {

    private final SessionalPlanJpaRepository sessionalPlanJpaRepository;

    @Override
    public List<SessionalPlan> loadAllByUserIdOrderBySessionalPlanIdDesc(Long userId) {
        return sessionalPlanJpaRepository
                .findByUser_userIdOrderBySessionalPlanIdDesc(userId)
                .stream()
                .map(SessionalPlanEntity::toModel)
                .toList();
    }
}