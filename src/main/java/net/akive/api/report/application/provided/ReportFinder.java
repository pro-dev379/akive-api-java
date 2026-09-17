package net.akive.api.report.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

public interface ReportFinder {

    PageResult<ReportHeaderResult> findHeaderPage(Long userId, PageCondition pageCondition);

    Report find(Long reportId, Long userId);

    List<ReportType> findAvailableReportTypes(Long userId, LocalDate reportOn) throws ResourceNotFoundException;
}
