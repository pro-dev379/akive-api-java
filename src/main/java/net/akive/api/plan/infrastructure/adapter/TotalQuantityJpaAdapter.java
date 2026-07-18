package net.akive.api.plan.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.infrastructure.repository.TotalQuantityJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class TotalQuantityJpaAdapter implements ValidateTotalQuantityPort {

    private final TotalQuantityJpaRepository totalQuantityJpaRepository;

    @Override
    public boolean existsBySessionalPlanId(Long sessionalPlanId) {
        return totalQuantityJpaRepository.existsBySessionalPlan_SessionalPlanId(sessionalPlanId);
    }
}
