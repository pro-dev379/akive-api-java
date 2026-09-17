package net.akive.api.report.application.required;

import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findByReportIdAndUserId(Long reportId, Long userId);

    boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);

    void delete(Report report);
}
