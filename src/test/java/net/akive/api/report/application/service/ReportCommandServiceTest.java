package net.akive.api.report.application.service;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceConflictError.REPORT_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.REPORT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportCreateCommand;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportFeedbackItemsUpdateCommand;
import static net.akive.api.report.domain.FeedbackType.HIGHLIGHT;
import static net.akive.api.report.domain.fixture.ReportFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    ReportRepository reportRepository;

    @Mock
    ReportValidationService reportValidationService;

    @InjectMocks
    ReportCommandService reportCommandService;

    @Test
    void 리포트를_생성한다() {
        User user = createReconstructedUser(USER_ID);
        ReportFeedback reportFeedback = createReportFeedback(HIGHLIGHT);
        ReportCreateCommand command = createReportCreateCommand(USER_ID, List.of(reportFeedback));
        Report savedReport = Report.reconstruct(
                REPORT_ID,
                user,
                command.reportOn(),
                command.reportType(),
                command.feedbackItems(),
                command.currentDate()
        );

        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));
        given(reportRepository.save(any(Report.class))).willReturn(savedReport);

        Long result = reportCommandService.create(command);

        assertThat(result).isEqualTo(REPORT_ID);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(reportValidationService).should()
                .validateCreatable(command.userId(), command.reportOn(), command.reportType());
        then(reportRepository).should().save(reportCaptor.capture());

        Report report = reportCaptor.getValue();
        assertThat(report.getUser()).isEqualTo(user);
        assertThat(report.getReportOn()).isEqualTo(command.reportOn());
        assertThat(report.getReportType()).isEqualTo(command.reportType());
        assertThat(report.getFeedbackItems()).containsExactly(reportFeedback);
    }

    @Test
    void 회원이_없으면_리포트를_생성할_수_없다() {
        ReportCreateCommand command = createReportCreateCommand(
                UNKNOWN_USER_ID,
                List.of(createReportFeedback(HIGHLIGHT))
        );
        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportCommandService.create(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(USER_NOT_FOUND);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(reportValidationService).should(never()).validateCreatable(any(), any(), any());
        then(reportRepository).should(never()).save(any());
    }

    @Test
    void 리포트_생성_검증에_실패하면_리포트를_저장하지_않는다() {
        User user = createReconstructedUser(USER_ID);
        ReportCreateCommand command = createReportCreateCommand(USER_ID, List.of(createReportFeedback(HIGHLIGHT)));
        given(userFinder.findByUserIdAndNotWithdrawn(command.userId())).willReturn(Optional.of(user));
        willThrow(new ResourceConflictException(REPORT_ALREADY_EXISTS))
                .given(reportValidationService)
                .validateCreatable(command.userId(), command.reportOn(), command.reportType());

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> reportCommandService.create(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(REPORT_ALREADY_EXISTS);

        then(userFinder).should().findByUserIdAndNotWithdrawn(command.userId());
        then(reportValidationService).should()
                .validateCreatable(command.userId(), command.reportOn(), command.reportType());
        then(reportRepository).should(never()).save(any());
    }

    @Test
    void 리포트를_삭제한다() {
        Report report = createReconstructedReport();
        given(reportRepository.findByReportIdAndUserId(REPORT_ID, USER_ID))
                .willReturn(Optional.of(report));

        reportCommandService.delete(REPORT_ID, USER_ID);

        then(reportRepository).should().findByReportIdAndUserId(REPORT_ID, USER_ID);
        then(reportRepository).should().delete(report);
    }

    @Test
    void 리포트가_없으면_삭제할_수_없다() {
        given(reportRepository.findByReportIdAndUserId(UNKNOWN_REPORT_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportCommandService.delete(UNKNOWN_REPORT_ID, USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(REPORT_NOT_FOUND);

        then(reportRepository).should().findByReportIdAndUserId(UNKNOWN_REPORT_ID, USER_ID);
        then(reportRepository).should(never()).delete(any());
    }

    @Test
    void 리포트_피드백_목록을_수정한다() {
        User user = createReconstructedUser(USER_ID);
        Report report = createReconstructedReport(REPORT_ID, user);
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(USER_ID);
        given(reportRepository.findByReportIdAndUserId(command.reportId(), command.userId()))
                .willReturn(Optional.of(report));

        reportCommandService.updateFeedbackItems(command);

        then(reportRepository).should().findByReportIdAndUserId(command.reportId(), command.userId());
        then(reportRepository).should().save(report);

        assertThat(report.getFeedbackItems()).containsExactlyElementsOf(command.targetFeedbackItems());
    }

    @Test
    void 리포트가_없으면_피드백_목록을_수정할_수_없다() {
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(UNKNOWN_REPORT_ID, USER_ID);
        given(reportRepository.findByReportIdAndUserId(command.reportId(), command.userId()))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportCommandService.updateFeedbackItems(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(REPORT_NOT_FOUND);

        then(reportRepository).should().findByReportIdAndUserId(command.reportId(), command.userId());
        then(reportRepository).should(never()).save(any());
    }
}
