package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.ValidateSessionalPlanPort;
import net.akive.api.plan.application.port.out.ValidateTotalQuantityPort;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.exception.SessionalPlanException;
import org.springframework.stereotype.Component;

import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_NAME_ALREADY_EXISTS;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_CHANGE_NOT_ALLOWED;
import static net.akive.api.common.domain.DomainValidator.requireMaxLength;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

@Component
@RequiredArgsConstructor
class SessionalPlanPolicy {

    private final ValidateSessionalPlanPort validateSessionalPlanPort;
    private final ValidateTotalQuantityPort validateTotalQuantityPort;

    void validateCreatable(Long userId, String requestedSessionName) {
        String normalizedSessionName = validateSessionName(requestedSessionName);

        if (validateSessionalPlanPort.existsByUserIdAndSessionName(userId, normalizedSessionName)) {
            throw new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS);
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
            throw new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS);
        }
    }

    private void validatePeriodChangeAllowed(
            SessionalPlan currentSessionalPlan,
            SessionPeriod requestedSessionPeriod
    ) {
        if (!currentSessionalPlan.getSessionPeriod().equals(requestedSessionPeriod) &&
                validateTotalQuantityPort.existsBySessionalPlanId(currentSessionalPlan.getSessionalPlanId())) {
            throw new SessionalPlanException(SESSION_PERIOD_CHANGE_NOT_ALLOWED);
        }
    }

    private static String validateSessionName(String requestedSessionName) {
        return requireMaxLength(requireNotBlank(requestedSessionName), 100);
    }
}
