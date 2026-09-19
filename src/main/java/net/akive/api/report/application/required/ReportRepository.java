package net.akive.api.report.application.required;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    PageResult<ReportHeaderResult> findHeaderPageByUserId(Long userId, PageCondition pageCondition);

    Optional<Report> findByReportIdAndUserId(Long reportId, Long userId);

    List<Report> findAllByUserIdAndReportTypeAndReportOnBetween(
            Long userId,
            ReportType reportType,
            LocalDate from,
            LocalDate to
    );

    boolean existsByUserIdAndReportOnAndReportType(Long userId, LocalDate reportOn, ReportType reportType);

    void delete(Report report);
}
