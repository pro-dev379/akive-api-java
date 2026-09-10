package net.akive.api.plan.application.provided.sessionalplan.dto;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

public record SessionalPlanUpdateCommand(
        Long sessionalPlanId,
        Long userId,
        String targetSessionName,
        SessionPeriod targetSessionPeriod
) {

    public SessionalPlanUpdateCommand {
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        targetSessionName = requireMaxLength(requireNotBlank(targetSessionName), SESSION_NAME_MAX_LENGTH);
        requireNonNull(targetSessionPeriod);
    }

    public static SessionalPlanUpdateCommand of(
            Long sessionalPlanId,
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new SessionalPlanUpdateCommand(
                sessionalPlanId,
                userId,
                sessionName,
                new SessionPeriod(sessionStartOn, sessionEndOn)
        );
    }
}
