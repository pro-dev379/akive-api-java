package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

public record ReportFindResponse(
        Long reportId,
        LocalDate reportOn,
        ReportType reportType,
        ReportFeedbackResponse feedback
) {

    public static ReportFindResponse from(Report report) {
        return new ReportFindResponse(
                report.getReportId(),
                report.getReportOn(),
                report.getReportType(),
                ReportFeedbackResponse.from(report.getFeedbackItems())
        );
    }
}
