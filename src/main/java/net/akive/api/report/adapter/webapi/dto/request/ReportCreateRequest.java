package net.akive.api.report.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportCreateRequest(
        @NotNull LocalDate reportOn,
        @NotNull ReportType reportType,
        @NotNull @Valid ReportFeedbackRequest feedback
) {

    public ReportCreateCommand to(Long userId, LocalDateTime requestedAt) {
        return new ReportCreateCommand(
                userId,
                this.reportOn,
                this.reportType,
                this.feedback.to(),
                requestedAt
        );
    }
}
