package net.akive.api.report.application.provided.dto;

import net.akive.api.report.domain.ReportCoveragePeriod;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

public record ReportWritingStatusResult(
        ReportType reportType,
        LocalDate reportOn,
        ReportCoveragePeriod coveragePeriod,
        boolean isWritten
) {
}
