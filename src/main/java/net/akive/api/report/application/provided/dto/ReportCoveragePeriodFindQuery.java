package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record ReportCoveragePeriodFindQuery(
        Long userId,
        LocalDate reportOn,
        ReportType reportType
) {

    public ReportCoveragePeriodFindQuery {
        requirePositive(userId);
        requireNonNull(reportOn);
        requireNonNull(reportType);
    }
}
