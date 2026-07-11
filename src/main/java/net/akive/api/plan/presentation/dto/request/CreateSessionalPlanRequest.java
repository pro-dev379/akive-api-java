package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;

public record CreateSessionalPlanRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull SessionPeriodRequest sessionPeriod
) {

    public CreateSessionalPlanCommand to(Long userId) {
        return CreateSessionalPlanCommand.of(
                userId,
                sessionName,
                sessionPeriod.startOn(),
                sessionPeriod.endOn()
        );
    }
}
