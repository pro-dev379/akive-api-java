package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanCreator;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanCreateCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDeleteCommand;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class SessionalPlanCommandService implements SessionalPlanCreator {

    private final UserFinder userFinder;

    private final SessionalPlanRepository sessionalPlanRepository;
    private final SessionalPlanDomainService sessionalPlanDomainService;

    @Override
    public Long create(SessionalPlanCreateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException {
        User user = userFinder.find(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        SessionalPlan sessionalPlan = sessionalPlanDomainService.create(
                user,
                command.sessionName(),
                command.sessionPeriod()
        );

        SessionalPlan savedSessionalPlan = sessionalPlanRepository.save(sessionalPlan);

        return savedSessionalPlan.getSessionalPlanId();
    }

    @Override
    public void delete(SessionalPlanDeleteCommand command) throws ResourceNotFoundException {
        SessionalPlan sessionalPlan = findSessionalPlanOrThrow(command.sessionalPlanId(), command.userId());

        sessionalPlanRepository.delete(sessionalPlan);
    }

    @Override
    public void update(SessionalPlanUpdateCommand command)
            throws BusinessRuleViolationException, ResourceConflictException, ResourceNotFoundException {
        SessionalPlan sessionalPlan = findSessionalPlanOrThrow(command.sessionalPlanId(), command.userId());

        sessionalPlanDomainService.update(sessionalPlan, command.targetSessionName(), command.targetSessionPeriod());

        sessionalPlanRepository.save(sessionalPlan);
    }

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository
                .findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
