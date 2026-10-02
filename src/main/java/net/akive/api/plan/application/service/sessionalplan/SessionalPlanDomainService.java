package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;

@Component
@RequiredArgsConstructor
class SessionalPlanDomainService {

    private final SessionalPlanValidationService sessionalPlanValidationService;
    private final CurrentTimeProvider currentTimeProvider;

    SessionalPlan create(User user, String sessionName, SessionPeriod sessionPeriod) {
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());

        sessionalPlanValidationService.validateCreatable(user.getUserId(), sessionName, currentDate);

        return SessionalPlan.create(user, sessionName, sessionPeriod, currentDate);
    }

    void update(
            SessionalPlan currentSessionalPlan,
            String targetSessionName,
            SessionPeriod targetSessionPeriod
    ) {
        sessionalPlanValidationService.validateUpdatable(
                currentSessionalPlan,
                targetSessionName,
                targetSessionPeriod
        );

        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());

        currentSessionalPlan.update(targetSessionName, targetSessionPeriod, currentDate);
    }
}
