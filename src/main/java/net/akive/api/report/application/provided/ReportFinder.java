package net.akive.api.report.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;

import java.time.LocalDate;
import java.util.List;

public interface ReportFinder {

    Report find(Long reportId, Long userId);

    List<ReportType> findAvailableReportTypes(Long userId, LocalDate reportOn) throws ResourceNotFoundException;
}
