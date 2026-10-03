package net.akive.api.report.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.report.application.provided.ReportCreator;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.REPORT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;

@Service
@Transactional
@RequiredArgsConstructor
class ReportCommandService implements ReportCreator {

    private final UserFinder userFinder;
    private final ReportRepository reportRepository;

    private final CurrentTimeProvider currentTimeProvider;
    private final ReportValidationService reportValidationService;

    @Override
    public Long create(ReportCreateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException, ResourceConflictException {
        User user = findUserOrThrow(command.userId());

        reportValidationService.validateCreatable(command.userId(), command.reportOn(), command.reportType());

        Report report = Report.create(
                user,
                command.reportOn(),
                command.reportType(),
                command.feedbackItems(),
                toKoreanDate(currentTimeProvider.now())
        );

        Report savedReport = reportRepository.save(report);

        return savedReport.getReportId();
    }

    @Override
    public void delete(Long reportId, Long userId) {
        Report report = findReportOrThrow(reportId, userId);

        reportRepository.delete(report);
    }

    @Override
    public void updateFeedbackItems(ReportFeedbackItemsUpdateCommand command)
            throws BusinessRuleViolationException, ResourceNotFoundException {
        Report report = findReportOrThrow(command.reportId(), command.userId());

        report.updateFeedbackItems(command.targetFeedbackItems());

        reportRepository.save(report);
    }

    private User findUserOrThrow(Long userId) {
        return userFinder.find(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private Report findReportOrThrow(Long reportId, Long userId) {
        return reportRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(REPORT_NOT_FOUND));
    }
}
