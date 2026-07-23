package net.akive.api.plan.presentation.dto.request.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 20) String category
) {
}