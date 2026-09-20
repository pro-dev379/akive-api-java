package net.akive.api.report.domain.fixture;

import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.report.domain.FeedbackType;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.report.domain.ReportType;
import net.akive.api.user.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.report.domain.FeedbackSource.USER;
import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;

public class ReportFixture {

    private ReportFixture() {
    }


    public static final Long REPORT_ID = 1L;
    public static final Long UNKNOWN_REPORT_ID = Long.MAX_VALUE;
    public static final Long ANOTHER_REPORT_ID = 2L;
    public static final LocalDate REPORT_ON = LocalDate.of(2026, 1, 25);
    public static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 1, 25, 10, 0);
    public static final String FEEDBACK_CONTENT = "피드백";
    public static final String TARGET_HIGHLIGHT_FEEDBACK_CONTENT = "수정된 하이라이트";
    public static final String TARGET_IMPROVEMENT_FEEDBACK_CONTENT = "수정된 개선점";

    public static SessionPeriod createYear2026SessionPeriod() {
        return new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static Report createReport() {
        return createReport(createReconstructedUser());
    }

    public static Report createReport(User user) {
        return createReport(user, REPORT_ON);
    }

    public static Report createReport(User user, LocalDate reportOn) {
        return createReport(user, reportOn, WEEKLY, createReportFeedbackItems(), REQUESTED_AT);
    }

    public static Report createReport(LocalDate reportOn) {
        return createReport(reportOn, createReportFeedbackItems());
    }

    public static Report createReport(List<ReportFeedback> feedbackItems) {
        return createReport(REPORT_ON, feedbackItems);
    }

    public static Report createReport(LocalDate reportOn, List<ReportFeedback> feedbackItems) {
        return createReport(createReconstructedUser(), reportOn, WEEKLY, feedbackItems, REQUESTED_AT);
    }

    public static Report createReport(
            User user,
            LocalDate reportOn,
            ReportType reportType,
            List<ReportFeedback> feedbackItems,
            LocalDateTime requestedAt
    ) {
        return Report.create(user, reportOn, reportType, feedbackItems, requestedAt);
    }

    public static Report createReconstructedReport() {
        return createReconstructedReport(REPORT_ID);
    }

    public static Report createReconstructedReport(Long reportId) {
        return createReconstructedReport(reportId, createReconstructedUser());
    }

    public static Report createReconstructedReport(Long reportId, User user) {
        return createReconstructedReport(
                reportId,
                user,
                REPORT_ON,
                WEEKLY,
                createReportFeedbackItems(),
                REQUESTED_AT
        );
    }

    public static Report createReconstructedReport(LocalDate reportOn) {
        return createReconstructedReport(reportOn, createReportFeedbackItems());
    }

    public static Report createReconstructedReport(List<ReportFeedback> feedbackItems) {
        return createReconstructedReport(REPORT_ON, feedbackItems);
    }

    public static Report createReconstructedReport(LocalDate reportOn, List<ReportFeedback> feedbackItems) {
        return createReconstructedReport(
                REPORT_ID,
                createReconstructedUser(),
                reportOn,
                WEEKLY,
                feedbackItems,
                REQUESTED_AT
        );
    }

    public static Report createReconstructedReport(
            Long reportId,
            User user,
            LocalDate reportOn,
            ReportType reportType,
            List<ReportFeedback> feedbackItems,
            LocalDateTime requestedAt
    ) {
        return Report.reconstruct(reportId, user, reportOn, reportType, feedbackItems, requestedAt);
    }

    public static ReportFeedback createReportFeedback(FeedbackType feedbackType) {
        return createReportFeedback(feedbackType, 1, FEEDBACK_CONTENT);
    }

    public static ReportFeedback createReportFeedback(FeedbackType feedbackType, String content) {
        return createReportFeedback(feedbackType, 1, content);
    }

    public static ReportFeedback createReportFeedback(FeedbackType feedbackType, long sortOrder, String content) {
        return new ReportFeedback(feedbackType, USER, sortOrder, content);
    }

    public static List<ReportFeedback> createReportFeedbackItems() {
        return List.of(createReportFeedback(HIGHLIGHT), createReportFeedback(IMPROVEMENT));
    }

    public static List<ReportFeedback> createTargetReportFeedbackItems() {
        return List.of(
                createReportFeedback(HIGHLIGHT, TARGET_HIGHLIGHT_FEEDBACK_CONTENT),
                createReportFeedback(IMPROVEMENT, TARGET_IMPROVEMENT_FEEDBACK_CONTENT)
        );
    }
}
