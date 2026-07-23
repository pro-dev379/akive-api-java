package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.presentation.dto.request.common.SessionPeriodRequest;

public record CreateSessionalPlanRequest(
        @NotBlank @Size(max = 50) String sessionName,
        @NotNull @Valid SessionPeriodRequest sessionPeriod
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
