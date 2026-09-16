package net.akive.api.report.adapter.webapi.fixture;

import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.request.ReportFeedbackRequest;

import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportFixture.REPORT_ON;
import static net.akive.api.report.domain.ReportFixture.createReportFeedback;
import static net.akive.api.report.domain.ReportType.WEEKLY;

public class ReportRequestFixture {

    public static ReportCreateRequest createReportCreateRequest() {
        return new ReportCreateRequest(
                REPORT_ON,
                WEEKLY,
                createReportFeedbackRequest()
        );
    }

    public static ReportFeedbackRequest createReportFeedbackRequest() {
        return new ReportFeedbackRequest(
                List.of(createReportFeedback(HIGHLIGHT).content()),
                List.of(createReportFeedback(IMPROVEMENT).content()),
                List.of()
        );
    }

    public static ReportCreateRequest createEmptyReportFeedbackCreateRequest() {
        return new ReportCreateRequest(
                REPORT_ON,
                WEEKLY,
                new ReportFeedbackRequest(List.of(), List.of(), List.of())
        );
    }

    public static Stream<ReportCreateRequest> invalidCreateRequests() {
        return Stream.of(
                new ReportCreateRequest(null, WEEKLY, createReportFeedbackRequest()),
                new ReportCreateRequest(REPORT_ON, null, createReportFeedbackRequest()),
                new ReportCreateRequest(REPORT_ON, WEEKLY, null),
                new ReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(null, List.of(), List.of())
                ),
                new ReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(List.of(), null, List.of())
                ),
                new ReportCreateRequest(
                        REPORT_ON,
                        WEEKLY,
                        new ReportFeedbackRequest(List.of(), List.of(), null)
                )
        );
    }
}
