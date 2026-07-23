package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.UpdateSessionalPlanCommand;
import net.akive.api.plan.presentation.dto.request.common.SessionPeriodRequest;

public record UpdateSessionalPlanRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull @Valid SessionPeriodRequest sessionPeriod
) {

    public UpdateSessionalPlanCommand to(Long sessionalPlanId, Long userId) {
        return UpdateSessionalPlanCommand.of(
                sessionalPlanId,
                userId,
                this.sessionName,
                sessionPeriod.startOn(),
                sessionPeriod.endOn()
        );
    }
}
