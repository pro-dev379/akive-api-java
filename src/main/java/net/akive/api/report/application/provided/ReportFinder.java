package net.akive.api.report.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryResult;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryFindQuery;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

public interface ReportFinder {

    Report find(Long reportId, Long userId);

    List<ReportFeedbackHistoryResult> findFeedbackHistory(ReportFeedbackHistoryFindQuery query);

    PageResult<ReportHeaderResult> findHeaderPage(Long userId, PageCondition pageCondition);

    List<ReportType> findAvailableReportTypes(Long userId, LocalDate reportOn) throws ResourceNotFoundException;
}
