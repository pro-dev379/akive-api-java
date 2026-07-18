package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.DeleteSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.UpdateSessionalPlanUseCase;
import net.akive.api.plan.application.port.out.*;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.exception.PlanException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class CommandSessionalPlanService implements
        CreateSessionalPlanUseCase,
        DeleteSessionalPlanUseCase,
        UpdateSessionalPlanUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final SaveSessionalPlanPort saveSessionalPlanPort;
    private final DeleteSessionalPlanPort deleteSessionalPlanPort;
    private final SessionalPlanDomainService sessionalPlanDomainService;

    @Override
    public Long create(CreateSessionalPlanCommand command) {
        SessionalPlan sessionalPlan = sessionalPlanDomainService.createForUser(
                command.userId(),
                command.sessionName(),
                command.sessionPeriod()
        );

        SessionalPlan savedSessionalPlan = saveSessionalPlanPort.save(sessionalPlan);

        return savedSessionalPlan.getSessionalPlanId();
    }

    @Override
    public void delete(DeleteSessionalPlanCommand command) {
        long deletedCount = deleteSessionalPlanPort.deleteBySessionalPlanIdAndUserId(command.sessionalPlanId(), command.userId());
        if (deletedCount == 0) {
            throw new PlanException(SESSIONAL_PLAN_NOT_FOUND);
        }
    }

    @Override
    public void update(UpdateSessionalPlanCommand command) {
        SessionalPlan sessionalPlan = loadSessionalPlanOrThrow(command.sessionalPlanId(), command.userId());

        SessionalPlan updatedSessionalPlan = sessionalPlanDomainService.update(
                sessionalPlan,
                command.sessionName(),
                command.sessionPeriod()
        );

        saveSessionalPlanPort.save(updatedSessionalPlan);
    }

    private SessionalPlan loadSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new PlanException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
