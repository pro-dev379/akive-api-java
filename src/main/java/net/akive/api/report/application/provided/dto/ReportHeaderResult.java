package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record ReportHeaderResult(
        Long reportId,
        LocalDate reportOn,
        ReportType reportType
) {

    public ReportHeaderResult {
        requirePositive(reportId);
        requireNonNull(reportOn);
        requireNonNull(reportType);
    }
}