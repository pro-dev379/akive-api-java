package net.akive.api.report.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportType;
import net.akive.api.report.domain.ReportTypePolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.REPORT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ReportQueryService implements ReportFinder {

    private final SessionalPlanFinder sessionalPlanFinder;
    private final ReportRepository reportRepository;

    @Override
    public Report find(Long reportId, Long userId) {
        return findReportOrThrow(reportId, userId);
    }

    @Override
    public PageResult<ReportHeaderResult> findHeaderPage(Long userId, PageCondition pageCondition) {
        return reportRepository.findHeaderPageByUserId(userId, pageCondition);
    }

    @Override
    public List<ReportType> findAvailableReportTypes(Long userId, LocalDate reportOn)
            throws ResourceNotFoundException {
        List<SessionPeriod> sessionPeriods = sessionalPlanFinder.findSessionPeriodsByUserId(userId);

        validateReportOnWithinSessionPeriod(reportOn, sessionPeriods);

        return ReportTypePolicy.findAvailableReportTypes(sessionPeriods, reportOn);
    }

    private void validateReportOnWithinSessionPeriod(LocalDate reportOn, List<SessionPeriod> sessionPeriods) {
        boolean hasActiveSessionPeriod = sessionPeriods.stream()
                .anyMatch(sessionPeriod -> isWithinSessionPeriod(sessionPeriod, reportOn));

        if (!hasActiveSessionPeriod) {
            throw new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND);
        }
    }

    private boolean isWithinSessionPeriod(SessionPeriod sessionPeriod, LocalDate reportOn) {
        return !sessionPeriod.startOn().isAfter(reportOn)
                && !sessionPeriod.endOn().isBefore(reportOn);
    }

    private Report findReportOrThrow(Long reportId, Long userId) {
        return reportRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(REPORT_NOT_FOUND));
    }
}
