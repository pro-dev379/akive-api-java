package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.exception.business.BadRequestError.IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.BadRequestError.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ConflictError.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.PlanPolicy.*;

@Component
@RequiredArgsConstructor
class SessionalPlanValidator {

    private final ValidateSessionalPlanPort validateSessionalPlanPort;
    private final ValidateTotalQuantityPort validateTotalQuantityPort;

    void validateCreatable(Long userId, String requestedSessionName, LocalDate now) {
        String normalizedSessionName = validateSessionName(requestedSessionName);
        if (validateSessionalPlanPort.existsByUserIdAndSessionName(userId, normalizedSessionName)) {
            throw new ConflictException(SESSION_NAME_ALREADY_EXISTS);
        }

        int inProgressSessionalPlanCount = validateSessionalPlanPort.countInProgressByUserId(userId, now);
        if (inProgressSessionalPlanCount >= IN_PROGRESS_SESSIONAL_PLAN_MAX_COUNT) {
            throw new BadRequestException(IN_PROGRESS_SESSIONAL_PLAN_LIMIT_EXCEEDED);
        }
    }

    void validateUpdatable(
            SessionalPlan currentSessionalPlan,
            String requestedSessionName,
            SessionPeriod requestedSessionPeriod
    ) {
        String normalizedSessionName = validateSessionName(requestedSessionName);

        validateSessionNameNotDuplicated(
                currentSessionalPlan.getUserId(),
                normalizedSessionName,
                currentSessionalPlan.getSessionalPlanId()
        );

        validatePeriodChangeAllowed(currentSessionalPlan, requestedSessionPeriod);
    }

    private void validateSessionNameNotDuplicated(
            Long userId,
            String requestedSessionName,
            Long currentSessionalPlanId
    ) {
        if (validateSessionalPlanPort.existsByUserIdAndSessionNameAndSessionalPlanIdNot(
                userId,
                requestedSessionName,
                currentSessionalPlanId
        )) {
            throw new ConflictException(SESSION_NAME_ALREADY_EXISTS);
        }
    }

    private void validatePeriodChangeAllowed(
            SessionalPlan currentSessionalPlan,
            SessionPeriod requestedSessionPeriod
    ) {
        if (!currentSessionalPlan.getSessionPeriod().equals(requestedSessionPeriod) &&
                validateTotalQuantityPort.existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId())) {
            throw new BadRequestException(SESSION_PERIOD_CHANGE_NOT_ALLOWED);
        }
    }

    private static String validateSessionName(String requestedSessionName) {
        return requireMaxLength(requireNotBlank(requestedSessionName), SESSION_NAME_MAX_LENGTH);
    }
}
