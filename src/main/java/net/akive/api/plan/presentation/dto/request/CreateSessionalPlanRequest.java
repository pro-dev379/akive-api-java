package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;

import java.time.LocalDate;

public record CreateSessionalPlanRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull LocalDate sessionStartOn,
        @NotNull LocalDate sessionEndOn
) {

    public CreateSessionalPlanCommand toCommand(Long userId) {
        return new CreateSessionalPlanCommand(userId, sessionName, sessionStartOn, sessionEndOn);
    }
}
