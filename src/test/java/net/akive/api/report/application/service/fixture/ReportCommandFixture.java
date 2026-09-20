package net.akive.api.report.application.service.fixture;

import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;
import net.akive.api.report.domain.ReportFeedback;

import java.util.List;

import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.report.domain.fixture.ReportFixture.*;

public class ReportCommandFixture {

    private ReportCommandFixture() {
    }

    public static ReportCreateCommand createReportCreateCommand(Long userId) {
        return createReportCreateCommand(userId, createReportFeedbackItems());
    }

    public static ReportCreateCommand createReportCreateCommand(
            Long userId,
            List<ReportFeedback> feedbackItems
    ) {
        return new ReportCreateCommand(
                userId,
                REPORT_ON,
                WEEKLY,
                feedbackItems,
                REQUESTED_AT
        );
    }

    public static ReportFeedbackItemsUpdateCommand createReportFeedbackItemsUpdateCommand(Long userId) {
        return createReportFeedbackItemsUpdateCommand(REPORT_ID, userId);
    }

    public static ReportFeedbackItemsUpdateCommand createReportFeedbackItemsUpdateCommand(
            Long reportId,
            Long userId
    ) {
        return new ReportFeedbackItemsUpdateCommand(
                reportId,
                userId,
                createTargetReportFeedbackItems()
        );
    }
}
