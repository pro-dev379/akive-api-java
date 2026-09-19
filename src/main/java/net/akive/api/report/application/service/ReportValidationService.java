package net.akive.api.report.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.ReportSchedule;
import net.akive.api.report.domain.ReportType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_REPORT_TYPE;
import static net.akive.api.common.exception.business.ResourceConflictError.REPORT_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Component
@RequiredArgsConstructor
class ReportValidationService {

    private final SessionalPlanFinder sessionalPlanFinder;
    private final ReportRepository reportRepository;

    void validateCreatable(Long userId, LocalDate reportOn, ReportType reportType) {
        List<SessionPeriod> sessionPeriods = sessionalPlanFinder.findSessionPeriodsByUserId(userId);

        validateReportOnWithinSessionPeriod(reportOn, sessionPeriods);
        validateReportType(reportOn, reportType, sessionPeriods);
        validateReportUniqueness(userId, reportOn, reportType);
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

    private void validateReportType(
            LocalDate reportOn,
            ReportType reportType,
            List<SessionPeriod> sessionPeriods
    ) {
        List<ReportType> availableReportTypes =
                ReportSchedule.from(sessionPeriods).findAvailableReportTypes(reportOn);

        if (!availableReportTypes.contains(reportType)) {
            throw new BusinessRuleViolationException(INVALID_REPORT_TYPE);
        }
    }

    private void validateReportUniqueness(Long userId, LocalDate reportOn, ReportType reportType) {
        boolean isDuplicated =
                reportRepository.existsByUserIdAndReportOnAndReportType(userId, reportOn, reportType);

        if (isDuplicated) {
            throw new ResourceConflictException(REPORT_ALREADY_EXISTS);
        }
    }
}
