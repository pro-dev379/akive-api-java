package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;
import net.akive.api.plan.application.port.in.CreateTotalQuantityUseCase;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.SaveTotalQuantityPort;
import net.akive.api.plan.domain.TotalQuantityPlan;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.TotalQuantityPlanFactory;
import net.akive.api.plan.exception.PlanException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class TotalQuantityService implements CreateTotalQuantityUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final SaveTotalQuantityPort saveTotalQuantityPort;

    private final TotalQuantityPlanFactory totalQuantityPlanFactory = new TotalQuantityPlanFactory();

    @Override
    public Long create(CreateTotalQuantityCommand command) {
        SessionalPlan sessionalPlan = loadSessionalPlanOrThrow(command.sessionalPlanId(), command.userId());
        TotalQuantityPlan totalQuantityPlan = totalQuantityPlanFactory.create(
                sessionalPlan,
                command.subject(),
                command.totalQuantityAmount()
        );

        return saveTotalQuantityPort.save(totalQuantityPlan);
    }

    private SessionalPlan loadSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new PlanException(SESSIONAL_PLAN_NOT_FOUND));
    }
}