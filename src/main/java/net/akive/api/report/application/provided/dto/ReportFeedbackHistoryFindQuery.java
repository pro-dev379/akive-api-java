package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record ReportFeedbackHistoryFindQuery(
        Long userId,
        ReportType reportType,
        LocalDate from,
        LocalDate to
) {

    public ReportFeedbackHistoryFindQuery {
        requirePositive(userId);
        requireNonNull(reportType);
        requireStartNotAfterEnd(from, to);
    }
}
