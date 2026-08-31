package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
class SessionalPlanDomainService {

    private final SessionalPlanValidationService sessionalPlanValidationService;
    private final CurrentTimeProvider currentTimeProvider;

    SessionalPlan create(User user, String sessionName, SessionPeriod sessionPeriod) {
        LocalDateTime now = currentTimeProvider.now();

        sessionalPlanValidationService.validateCreatable(user.getUserId(), sessionName, now.toLocalDate());

        return SessionalPlan.create(user, sessionName, sessionPeriod, now);
    }

    void update(
            SessionalPlan currentSessionalPlan,
            String sessionName,
            SessionPeriod sessionPeriod
    ) {
        sessionalPlanValidationService.validateUpdatable(currentSessionalPlan, sessionName, sessionPeriod);

        currentSessionalPlan.update(sessionName, sessionPeriod, currentTimeProvider.now());
    }
}
