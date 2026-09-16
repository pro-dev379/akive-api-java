package net.akive.api.report.application.provided.dto;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.REPORT_DATE_IN_FUTURE;

public record ReportCreateCommand(
        Long userId,
        LocalDate reportOn,
        ReportType reportType,
        List<ReportFeedback> feedbackItems,
        LocalDateTime requestedAt
) {

    public ReportCreateCommand {
        requirePositive(userId);
        requireNonNull(reportOn);
        requireNonNull(reportType);
        requireNotEmpty(feedbackItems);
        requireNonNull(requestedAt);

        if (reportOn.isAfter(requestedAt.toLocalDate())) {
            throw new BusinessRuleViolationException(REPORT_DATE_IN_FUTURE);
        }
    }
}
