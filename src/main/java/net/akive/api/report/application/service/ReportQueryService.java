package net.akive.api.report.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.application.provided.dto.*;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportCoveragePeriod;
import net.akive.api.report.domain.ReportSchedule;
import net.akive.api.report.domain.ReportType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<ReportFeedbackHistoryResult> findFeedbackHistory(ReportFeedbackHistoryFindQuery query) {
        List<Report> reports = reportRepository.findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
                query.userId(),
                query.reportType(),
                query.startOn(),
                query.endOn()
        );

        return reports.stream()
                .map(report -> new ReportFeedbackHistoryResult(
                        report.getReportOn(),
                        report.getFeedbackItems()
                ))
                .toList();
    }

    @Override
    public ReportCoveragePeriod findCoveragePeriod(ReportCoveragePeriodFindQuery query)
            throws ResourceNotFoundException, BusinessRuleViolationException {
        List<SessionPeriod> sessionPeriods = sessionalPlanFinder.findSessionPeriodsByUserId(query.userId());

        validateReportOnWithinSessionPeriod(query.reportOn(), sessionPeriods);

        return ReportSchedule.from(sessionPeriods).findCoveragePeriod(query.reportOn(), query.reportType());
    }

    @Override
    public List<ReportWritingStatusResult> findWritingStatuses(Long userId, LocalDate baseDate)
            throws ResourceNotFoundException {
        List<SessionPeriod> sessionPeriods = sessionalPlanFinder.findSessionPeriodsByUserId(userId);

        validateReportOnWithinSessionPeriod(baseDate, sessionPeriods);

        ReportSchedule reportSchedule = ReportSchedule.from(sessionPeriods);
        List<ReportWritingStatusCandidate> writingStatusCandidates = new ArrayList<>();

        for (ReportType reportType : ReportType.values()) {
            reportSchedule.findReportingCoveragePeriods(baseDate, reportType)
                    .stream()
                    .map(coveragePeriod -> new ReportWritingStatusCandidate(reportType, coveragePeriod))
                    .forEach(writingStatusCandidates::add);
        }

        Set<ReportKey> writtenReportKeys = findWrittenReportKeys(userId, writingStatusCandidates);

        return writingStatusCandidates.stream()
                .map(candidate -> toWritingStatus(
                        candidate.reportType(),
                        candidate.coveragePeriod(),
                        writtenReportKeys
                ))
                .toList();
    }

    @Override
    public List<ReportType> findAvailableReportTypes(Long userId, LocalDate reportOn)
            throws ResourceNotFoundException {
        List<SessionPeriod> sessionPeriods = sessionalPlanFinder.findSessionPeriodsByUserId(userId);

        validateReportOnWithinSessionPeriod(reportOn, sessionPeriods);

        return ReportSchedule.from(sessionPeriods).findAvailableReportTypes(reportOn);
    }

    private Set<ReportKey> findWrittenReportKeys(
            Long userId,
            List<ReportWritingStatusCandidate> writingStatusCandidates
    ) {
        if (writingStatusCandidates.isEmpty()) {
            return Set.of();
        }

        LocalDate startOn = findStartOn(writingStatusCandidates);
        LocalDate endOn = findEndOn(writingStatusCandidates);

        return reportRepository.findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
                        userId,
                        startOn,
                        endOn
                )
                .stream()
                .map(reportHeader -> new ReportKey(reportHeader.reportType(), reportHeader.reportOn()))
                .collect(Collectors.toSet());
    }

    private LocalDate findStartOn(List<ReportWritingStatusCandidate> writingStatusCandidates) {
        return writingStatusCandidates.stream()
                .map(candidate -> candidate.coveragePeriod().endOn())
                .min(LocalDate::compareTo)
                .orElseThrow();
    }

    private LocalDate findEndOn(List<ReportWritingStatusCandidate> writingStatusCandidates) {
        return writingStatusCandidates.stream()
                .map(candidate -> candidate.coveragePeriod().endOn())
                .max(LocalDate::compareTo)
                .orElseThrow();
    }

    private ReportWritingStatusResult toWritingStatus(
            ReportType reportType,
            ReportCoveragePeriod coveragePeriod,
            Set<ReportKey> writtenReportKeys
    ) {
        ReportKey reportKey = new ReportKey(reportType, coveragePeriod.endOn());

        return new ReportWritingStatusResult(
                reportType,
                coveragePeriod.endOn(),
                coveragePeriod,
                writtenReportKeys.contains(reportKey)
        );
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

    private record ReportKey(ReportType reportType, LocalDate reportOn) {
    }

    private record ReportWritingStatusCandidate(ReportType reportType, ReportCoveragePeriod coveragePeriod) {
    }
}
