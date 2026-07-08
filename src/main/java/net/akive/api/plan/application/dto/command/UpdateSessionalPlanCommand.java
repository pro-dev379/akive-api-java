package net.akive.api.plan.application.dto.command;

import java.time.LocalDate;

public record UpdateSessionalPlanCommand(
        Long sessionalPlanId,
        Long userId,
        String sessionName,
        LocalDate sessionStartOn,
        LocalDate sessionEndOn
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
                sessionStartOn,
                sessionEndOn
        );
    }
}
