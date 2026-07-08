package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;

import java.time.LocalDate;

public record UpdateSessionalPlanRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull LocalDate sessionStartOn,
        @NotNull LocalDate sessionEndOn
) {

    public UpdateSessionalPlanCommand toCommand(Long sessionalPlanId, Long userId) {
        return new UpdateSessionalPlanCommand(
                sessionalPlanId,
                userId,
                this.sessionName,
                this.sessionStartOn,
                this.sessionEndOn
        );
    }
}
