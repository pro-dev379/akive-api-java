package net.akive.api.plan.adapter.webapi.sessionalplan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.akive.api.plan.adapter.webapi.common.dto.request.SessionPeriodRequest;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanUpdateCommand;

import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

public record SessionalPlanUpdateRequest(
        @NotBlank @Size(max = SESSION_NAME_MAX_LENGTH) String targetSessionName,
        @NotNull @Valid SessionPeriodRequest targetSessionPeriod
) {

    public SessionalPlanUpdateCommand to(Long sessionalPlanId, Long userId) {
        return SessionalPlanUpdateCommand.of(
                sessionalPlanId,
                userId,
                this.targetSessionName,
                this.targetSessionPeriod.startOn(),
                this.targetSessionPeriod.endOn()
        );
    }
}
