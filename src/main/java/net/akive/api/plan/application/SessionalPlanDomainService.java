package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
class SessionalPlanDomainService {

    private final SessionalPlanValidator sessionalPlanValidator;
    private final CurrentTimeProvider currentTimeProvider;

    SessionalPlan createForUser(
            Long userId,
            String requestedSessionName,
            SessionPeriod requestedSessionPeriod
    ) {
        LocalDateTime now = currentTimeProvider.now();
        sessionalPlanValidator.validateCreatable(userId, requestedSessionName, now.toLocalDate());

        return SessionalPlan.createForUser(
                userId,
                requestedSessionName,
                requestedSessionPeriod,
                now
        );
    }

    SessionalPlan update(
            SessionalPlan currentSessionalPlan,
            String requestedSessionName,
            SessionPeriod requestedSessionPeriod
    ) {
        sessionalPlanValidator.validateUpdatable(
                currentSessionalPlan,
                requestedSessionName,
                requestedSessionPeriod
        );

        return currentSessionalPlan.update(
                requestedSessionName,
                requestedSessionPeriod,
                currentTimeProvider.now()
        );
    }
}
