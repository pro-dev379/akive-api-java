package net.akive.api.report.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;

public record ReportFeedbackUpdateRequest(@NotNull @Valid ReportFeedbackRequest targetFeedback) {

    public ReportFeedbackItemsUpdateCommand to(Long reportId, Long userId) {
        return new ReportFeedbackItemsUpdateCommand(reportId, userId, targetFeedback.to());
    }
}
