package net.akive.api.report.adapter.webapi.fixture;

import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.request.ReportFeedbackRequest;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportFixture.createReportFeedback;
import static net.akive.api.report.domain.ReportType.WEEKLY;

public class ReportRequestFixture {

    public static ReportCreateRequest createReportCreateRequest() {
        return createReportCreateRequest(REPORT_ON, WEEKLY, createReportFeedbackRequest());
    }

    public static ReportCreateRequest createReportCreateRequest(ReportFeedbackRequest reportFeedbackRequest) {
        return createReportCreateRequest(REPORT_ON, WEEKLY, reportFeedbackRequest);
    }

    public static ReportCreateRequest createReportCreateRequest(LocalDate reportOn) {
        return createReportCreateRequest(reportOn, WEEKLY, createReportFeedbackRequest());
    }

    private static ReportCreateRequest createReportCreateRequest(
            LocalDate reportOn,
            ReportType reportType,
            ReportFeedbackRequest reportFeedbackRequest
    ) {
        return new ReportCreateRequest(reportOn, reportType, reportFeedbackRequest);
    }

    public static ReportFeedbackRequest createReportFeedbackRequest() {
        return new ReportFeedbackRequest(
                List.of(createReportFeedback(HIGHLIGHT).content()),
                List.of(createReportFeedback(IMPROVEMENT).content()),
                List.of()
        );
    }

    public static ReportFeedbackRequest createEmptyReportFeedbackRequest() {
        return new ReportFeedbackRequest(List.of(), List.of(), List.of());
    }

    public static Stream<ReportCreateRequest> invalidCreateRequests() {
        return Stream.of(
                createReportCreateRequest(null, WEEKLY, createReportFeedbackRequest()),
                createReportCreateRequest(REPORT_ON, null, createReportFeedbackRequest()),
                createReportCreateRequest(REPORT_ON, WEEKLY, null),
                createReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(null, List.of(), List.of())
                ),
                createReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(List.of(), null, List.of())
                ),
                createReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(List.of(), List.of(), null)
                )
        );
    }
}
