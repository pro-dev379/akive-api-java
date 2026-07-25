package net.akive.api.plan.presentation.dto.request.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static net.akive.api.plan.domain.PlanPolicy.SUBJECT_CATEGORY_MAX_LENGTH;
import static net.akive.api.plan.domain.PlanPolicy.SUBJECT_NAME_MAX_LENGTH;

public record SubjectRequest(
        @NotBlank @Size(max = SUBJECT_NAME_MAX_LENGTH) String name,
        @NotBlank @Size(max = SUBJECT_CATEGORY_MAX_LENGTH) String category
) {
}