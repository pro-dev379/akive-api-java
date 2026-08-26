package net.akive.api.plan.adapter.webapi.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static net.akive.api.plan.domain.policy.PlanPolicy.SUBJECT_CATEGORY_MAX_LENGTH;
import static net.akive.api.plan.domain.policy.PlanPolicy.SUBJECT_NAME_MAX_LENGTH;

public record SubjectRequest(
        @NotBlank @Size(max = SUBJECT_NAME_MAX_LENGTH) String name,
        @NotBlank @Size(max = SUBJECT_CATEGORY_MAX_LENGTH) String category
) {
}
