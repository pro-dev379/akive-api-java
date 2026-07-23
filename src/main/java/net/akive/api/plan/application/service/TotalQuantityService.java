package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.port.in.CreateTotalQuantityUseCase;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.domain.TotalQuantityPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class TotalQuantityService implements CreateTotalQuantityUseCase {

    private final TotalQuantityDomainService totalQuantityDomainService;
    private final SaveTotalQuantityPort saveTotalQuantityPort;

    @Override
    public Long create(CreateTotalQuantityCommand command) {
        TotalQuantityPlan totalQuantityPlan = totalQuantityDomainService.create(
                command.sessionalPlanId(),
                command.userId(),
                command.subject(),
                command.totalQuantityAmount()
        );

        return saveTotalQuantityPort.save(totalQuantityPlan);
    }
}