package net.akive.api.plan.application.dto.command;

import java.time.LocalDate;

public record CreateSessionalPlanCommand(
        Long userId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
) {

    public static CreateSessionalPlanCommand of(
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return new CreateSessionalPlanCommand(userId, sessionName, sessionStartOn, sessionEndOn);
    }
}
