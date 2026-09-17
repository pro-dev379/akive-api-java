package net.akive.api.report.domain;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.report.domain.FeedbackSource.USER;
import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.FeedbackType.IMPROVEMENT;

public class ReportFixture {


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
