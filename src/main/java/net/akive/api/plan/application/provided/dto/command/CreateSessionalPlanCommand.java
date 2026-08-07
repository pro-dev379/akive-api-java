package net.akive.api.plan.application.provided.dto.command;

import net.akive.api.plan.domain.SessionPeriod;

import java.time.LocalDate;

public record CreateSessionalPlanCommand(
        Long userId,
        String sessionName,
        SessionPeriod sessionPeriod
) {

    public static CreateSessionalPlanCommand of(
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new CreateSessionalPlanCommand(
                userId,
                sessionName,
                new SessionPeriod(sessionStartOn, sessionEndOn)
        );
    }
}
