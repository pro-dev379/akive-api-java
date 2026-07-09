package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionalPlan;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SessionalPlanDomainService {

    private final SessionalPlanPolicy sessionalPlanPolicy;
    private final CurrentTimeProvider currentTimeProvider;

    SessionalPlan createForUser(
            Long userId,
            String requestedSessionName,
            SessionPeriod requestedSessionPeriod
    ) {
        sessionalPlanPolicy.validateCreatable(userId, requestedSessionName);

        return SessionalPlan.createForUser(
                userId,
                requestedSessionName,
                requestedSessionPeriod,
                currentTimeProvider.now()
        );
    }

    SessionalPlan update(
            SessionalPlan currentSessionalPlan,
            String requestedSessionName,
            SessionPeriod requestedSessionPeriod
    ) {
        sessionalPlanPolicy.validateUpdatable(
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
