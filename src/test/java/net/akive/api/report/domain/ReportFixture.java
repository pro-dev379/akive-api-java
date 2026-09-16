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
    public static final LocalDate REPORT_ON = LocalDate.of(2026, 1, 25);
    public static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 1, 25, 10, 0);

    public static SessionPeriod createYear2026SessionPeriod() {
        return new SessionPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
    }

    public static ReportFeedback createReportFeedback(FeedbackType feedbackType) {
        return createReportFeedback(feedbackType, 1);
    }

    public static ReportFeedback createReportFeedback(FeedbackType feedbackType, long sortOrder) {
        return new ReportFeedback(feedbackType, USER, sortOrder, "피드백");
    }

    public static List<ReportFeedback> createReportFeedbackItems() {
        return List.of(createReportFeedback(HIGHLIGHT), createReportFeedback(IMPROVEMENT));
    }
}
