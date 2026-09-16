package net.akive.api.report.application.required;

import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;

public interface ReportRepository {

    Report save(Report report);

    boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);
}
