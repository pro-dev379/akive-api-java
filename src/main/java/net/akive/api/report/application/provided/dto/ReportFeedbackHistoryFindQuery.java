package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record ReportFeedbackHistoryFindQuery(
        Long userId,
        ReportType reportType,
        LocalDate startOn,
        LocalDate endOn
) {

    public ReportFeedbackHistoryFindQuery {
        requirePositive(userId);
        requireNonNull(reportType);
        requireStartNotAfterEnd(startOn, endOn);
    }
}
