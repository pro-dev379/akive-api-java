package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryResult;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ReportFeedbackHistoryFindResponse(Map<LocalDate, ReportFeedbackResponse> feedbackHistory) {

    public static ReportFeedbackHistoryFindResponse from(
            List<ReportFeedbackHistoryResult> feedbackHistoryResults
    ) {
        Map<LocalDate, ReportFeedbackResponse> feedbackHistory = new LinkedHashMap<>();

        for (ReportFeedbackHistoryResult feedbackHistoryResult : feedbackHistoryResults) {
            LocalDate reportOn = feedbackHistoryResult.reportOn();

            if (feedbackHistory.containsKey(reportOn)) {
                throw new IllegalStateException("Duplicate reportOn: " + reportOn);
            }

            feedbackHistory.put(reportOn, ReportFeedbackResponse.from(feedbackHistoryResult.feedbackItems()));
        }

        return new ReportFeedbackHistoryFindResponse(feedbackHistory);
    }
}
