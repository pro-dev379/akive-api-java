package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportFeedback;

import java.time.LocalDate;
import java.util.List;

public record ReportFeedbackHistoryResult(
        LocalDate reportOn,
        List<ReportFeedback> feedbackItems
) {
}
