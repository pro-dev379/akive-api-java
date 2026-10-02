package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.domain.DomainValidator.*;

public record ReportCreateCommand(
        Long userId,
        LocalDate reportOn,
        ReportType reportType,
        List<ReportFeedback> feedbackItems
) {

    public ReportCreateCommand {
        requirePositive(userId);
        requireNonNull(reportOn);
        requireNonNull(reportType);
        requireNotEmpty(feedbackItems);
    }
}
