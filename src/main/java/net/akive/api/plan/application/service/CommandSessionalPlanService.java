package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.provided.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.provided.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.provided.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.application.provided.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.provided.DeleteSessionalPlanUseCase;
import net.akive.api.plan.application.provided.UpdateSessionalPlanUseCase;
import net.akive.api.plan.application.required.DeleteSessionalPlanPort;
import net.akive.api.plan.application.required.LoadSessionalPlanPort;
import net.akive.api.plan.application.required.SaveSessionalPlanPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.common.exception.business.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;

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
            throw new NotFoundException(SESSIONAL_PLAN_NOT_FOUND);
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
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
