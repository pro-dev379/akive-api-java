package net.akive.api.plan.application.provided.sessionalplan.dto;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record SessionalPlanUpdateCommand(
        Long sessionalPlanId,
        Long userId,
        String sessionName,
        SessionPeriod sessionPeriod
) {

    public SessionalPlanUpdateCommand {
        requirePositive(sessionalPlanId);
        requirePositive(userId);
        sessionName = requireNotBlank(sessionName);
        requireNonNull(sessionPeriod);
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
