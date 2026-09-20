package net.akive.api.report.adapter.webapi.dto.response;

import net.akive.api.report.application.provided.dto.ReportWritingStatusResult;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.report.domain.ReportType.*;

public record ReportWritingStatusesFindResponse(
        List<ReportWritingStatusResponse> dailyReports,
        List<ReportWritingStatusResponse> weeklyReports,
        List<ReportWritingStatusResponse> monthlyReports,
        List<ReportWritingStatusResponse> biannualReports,
        List<ReportWritingStatusResponse> annualReports
) {

    public static ReportWritingStatusesFindResponse from(List<ReportWritingStatusResult> reportWritingStatuses) {
        return new ReportWritingStatusesFindResponse(
                toResponses(reportWritingStatuses, DAILY),
                toResponses(reportWritingStatuses, WEEKLY),
                toResponses(reportWritingStatuses, MONTHLY),
                toResponses(reportWritingStatuses, BIANNUALLY),
                toResponses(reportWritingStatuses, ANNUALLY)
        );
    }

    private static List<ReportWritingStatusResponse> toResponses(
            List<ReportWritingStatusResult> reportWritingStatuses,
            ReportType reportType
    ) {
        return reportWritingStatuses.stream()
                .filter(reportWritingStatus -> reportWritingStatus.reportType() == reportType)
                .map(ReportWritingStatusResponse::from)
                .toList();
    }

    public record ReportWritingStatusResponse(LocalDate reportOn, boolean isWritten) {

        public static ReportWritingStatusResponse from(ReportWritingStatusResult result) {
            return new ReportWritingStatusResponse(result.reportOn(), result.isWritten());
        }
    }
}
