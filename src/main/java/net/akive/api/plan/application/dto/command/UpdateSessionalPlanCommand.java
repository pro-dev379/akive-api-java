package net.akive.api.plan.application.dto.command;

import net.akive.api.plan.domain.SessionPeriod;

import java.time.LocalDate;

public record UpdateSessionalPlanCommand(
        Long sessionalPlanId,
        Long userId,
        String sessionName,
        SessionPeriod sessionPeriod
) {

    public static UpdateSessionalPlanCommand of(
            Long sessionalPlanId,
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new UpdateSessionalPlanCommand(
                sessionalPlanId,
                userId,
                sessionName,
                new SessionPeriod(sessionStartOn, sessionEndOn)
        );
    }
}
