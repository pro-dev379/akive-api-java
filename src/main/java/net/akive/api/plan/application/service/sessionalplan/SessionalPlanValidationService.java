package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ResourceConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.policy.PlanPolicy.IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

@Component
@RequiredArgsConstructor
class SessionalPlanValidationService {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;

    void validateCreatable(Long userId, String sessionName, LocalDate requestedAt) {
        String normalizedSessionName = validateSessionName(sessionName);

        validateSessionNameUniqueForCreate(userId, normalizedSessionName);

        validateInProgressSessionalPlanLimit(userId, requestedAt);
    }

    void validateUpdatable(
            SessionalPlan currentSessionalPlan,
            String targetSessionName,
            SessionPeriod targetSessionPeriod
    ) {
        String normalizedSessionName = validateSessionName(targetSessionName);

        validateSessionNameUniqueForUpdate(
                currentSessionalPlan.getUser().getUserId(),
                normalizedSessionName,
                currentSessionalPlan.getSessionalPlanId()
        );

        validatePeriodChangeAllowed(currentSessionalPlan, targetSessionPeriod);
    }

    private String validateSessionName(String requestedSessionName) {
        return requireMaxLength(requireNotBlank(requestedSessionName), SESSION_NAME_MAX_LENGTH);
    }

    private void validateSessionNameUniqueForCreate(Long userId, String sessionName) {
        boolean isDuplicated = sessionalPlanRepository.existsByUserIdAndSessionName(userId, sessionName);

        if (isDuplicated) {
            throw new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS);
        }
    }

    private void validateSessionNameUniqueForUpdate(
            Long userId,
            String sessionName,
            Long currentSessionalPlanId
    ) {
        boolean isDuplicated = sessionalPlanRepository.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                sessionName,
                currentSessionalPlanId
        );

        if (isDuplicated) {
            throw new ResourceConflictException(SESSION_NAME_ALREADY_EXISTS);
        }
    }

    private void validateInProgressSessionalPlanLimit(Long userId, LocalDate requestedAt) {
        long inProgressSessionalPlanCount = sessionalPlanRepository.countInProgressByUserId(userId, requestedAt);

        if (inProgressSessionalPlanCount >= IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT) {
            throw new BusinessRuleViolationException(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED);
        }
    }

    private void validatePeriodChangeAllowed(
            SessionalPlan currentSessionalPlan,
            SessionPeriod targetSessionPeriod
    ) {
        boolean isPeriodChanged = !currentSessionalPlan.getSessionPeriod().equals(targetSessionPeriod);
        if (!isPeriodChanged) {
            return;
        }

        boolean hasTotalQuantity = totalQuantityRepository.existsBySessionalPlanId(
                currentSessionalPlan.getSessionalPlanId()
        );

        if (hasTotalQuantity) {
            throw new BusinessRuleViolationException(SESSION_PERIOD_CHANGE_NOT_ALLOWED);
        }
    }
}
