package net.akive.api.plan.application.dto.command;

import java.time.LocalDate;

public record SessionalPlanCreateCommand(
        Long userId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
) {

    public static SessionalPlanCreateCommand of(
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new SessionalPlanCreateCommand(userId, sessionName, sessionStartOn, sessionEndOn);
    }
}
