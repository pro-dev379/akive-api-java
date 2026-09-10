package net.akive.api.plan.application.provided.sessionalplan.dto;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

public record SessionalPlanCreateCommand(
        Long userId,
        String sessionName,
        SessionPeriod sessionPeriod
) {

    public SessionalPlanCreateCommand {
        requirePositive(userId);
        sessionName = requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH);
        requireNonNull(sessionPeriod);
    }

    public static SessionalPlanCreateCommand of(
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new SessionalPlanCreateCommand(
                userId,
                sessionName,
                new SessionPeriod(sessionStartOn, sessionEndOn)
        );
    }
}
