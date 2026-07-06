package net.akive.api.plan.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.SaveSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.infrastructure.entity.SessionalPlanEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionalPlanPortJpaAdapter implements LoadSessionalPlanPort, SaveSessionalPlanPort, ValidateSessionalPlanPort {

    private final SessionalPlanJpaRepository sessionalPlanJpaRepository;

    @Override
    public List<SessionalPlan> loadAllByUserIdOrderBySessionalPlanIdDesc(Long userId) {
        return sessionalPlanJpaRepository
                .findByUser_userIdOrderBySessionalPlanIdDesc(userId)
                .stream()
                .map(SessionalPlanEntity::toModel)
                .toList();
    }

    @Override
    public SessionalPlan save(SessionalPlan sessionalPlan) {
        return sessionalPlanJpaRepository.save(SessionalPlanEntity.fromModel(sessionalPlan)).toModel();
    }

    @Override
    public boolean existsByUserIdAndSessionName(Long userId, String sessionName) {
        return sessionalPlanJpaRepository.existsByUser_userIdAndSessionName(userId, sessionName);
    }
}