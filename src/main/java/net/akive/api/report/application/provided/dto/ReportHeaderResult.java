package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

public record ReportHeaderResult(
        Long reportId,
        LocalDate reportOn,
        ReportType reportType
) {
}