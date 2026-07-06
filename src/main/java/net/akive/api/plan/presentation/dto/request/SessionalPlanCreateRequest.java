package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.application.dto.command.SessionalPlanCreateCommand;

import java.time.LocalDate;

public record SessionalPlanCreateRequest(
        @NotBlank @Size(max = 100) String sessionName,
        @NotNull LocalDate sessionStartOn,
        @NotNull LocalDate sessionEndOn
) {

    public SessionalPlanCreateCommand toCommand(Long userId) {
        return new SessionalPlanCreateCommand(userId, sessionName, sessionStartOn, sessionEndOn);
    }
}
