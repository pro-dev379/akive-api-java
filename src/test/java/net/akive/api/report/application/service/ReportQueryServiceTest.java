package net.akive.api.report.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryFindQuery;
import net.akive.api.report.application.provided.dto.ReportFeedbackHistoryResult;
import net.akive.api.report.application.provided.dto.ReportHeaderResult;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.report.domain.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.REPORT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.report.domain.ReportFixture.*;
import static net.akive.api.report.domain.ReportType.MONTHLY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReportQueryServiceTest {

    @Mock
    SessionalPlanFinder sessionalPlanFinder;

    @Mock
    ReportRepository reportRepository;

    @InjectMocks
    ReportQueryService reportQueryService;

    @Test
    void 리포트_헤더_목록을_페이징_조회한다() {
        PageCondition pageCondition = PageCondition.of(DEFAULT_PAGE, DEFAULT_SIZE, "reportOn", DESC);
        ReportHeaderResult reportHeader = new ReportHeaderResult(REPORT_ID, REPORT_ON, WEEKLY);
        PageResult<ReportHeaderResult> reportHeaderPage = createPageResult(List.of(reportHeader));
        given(reportRepository.findHeaderPageByUserId(USER_ID, pageCondition))
                .willReturn(reportHeaderPage);

        PageResult<ReportHeaderResult> foundReportHeaderPage =
                reportQueryService.findHeaderPage(USER_ID, pageCondition);

        assertThat(foundReportHeaderPage).isEqualTo(reportHeaderPage);

        then(reportRepository).should().findHeaderPageByUserId(USER_ID, pageCondition);
    }

    @Test
    void 리포트를_조회한다() {
        Report report = createReconstructedReport();
        given(reportRepository.findByReportIdAndUserId(REPORT_ID, USER_ID))
                .willReturn(Optional.of(report));

        Report foundReport = reportQueryService.find(REPORT_ID, USER_ID);

        assertThat(foundReport).isEqualTo(report);

        then(reportRepository).should().findByReportIdAndUserId(REPORT_ID, USER_ID);
    }

    @Test
    void 리포트_피드백_히스토리를_조회한다() {
        LocalDate from = REPORT_ON.minusDays(1);
        LocalDate to = REPORT_ON;
        ReportFeedbackHistoryFindQuery query = new ReportFeedbackHistoryFindQuery(USER_ID, WEEKLY, from, to);
        List<ReportFeedback> firstFeedbackItems = createReportFeedbackItems();
        List<ReportFeedback> secondFeedbackItems = createReportFeedbackItems();
        Report firstReport = createReconstructedReport(from, firstFeedbackItems);
        Report secondReport = createReconstructedReport(to, secondFeedbackItems);
        given(reportRepository.findAllByUserIdAndReportTypeAndReportOnBetween(USER_ID, WEEKLY, from, to))
                .willReturn(List.of(firstReport, secondReport));

        List<ReportFeedbackHistoryResult> feedbackHistory = reportQueryService.findFeedbackHistory(query);

        assertThat(feedbackHistory)
                .containsExactly(
                        new ReportFeedbackHistoryResult(from, firstFeedbackItems),
                        new ReportFeedbackHistoryResult(to, secondFeedbackItems)
                );
        then(reportRepository).should()
                .findAllByUserIdAndReportTypeAndReportOnBetween(USER_ID, WEEKLY, from, to);
    }

    @Test
    void 리포트가_없으면_조회에_실패한다() {
        given(reportRepository.findByReportIdAndUserId(UNKNOWN_REPORT_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.find(UNKNOWN_REPORT_ID, USER_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(REPORT_NOT_FOUND);

        then(reportRepository).should().findByReportIdAndUserId(UNKNOWN_REPORT_ID, USER_ID);
    }

    @Test
    void 작성_가능한_리포트_타입을_조회한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportQueryService.findAvailableReportTypes(USER_ID, REPORT_ON);

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 리포트_날짜와_관련된_세셔널_플랜이_없으면_작성_가능한_리포트_타입_조회에_실패한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.findAvailableReportTypes(USER_ID, LocalDate.of(2024, 1, 1)))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }
}
