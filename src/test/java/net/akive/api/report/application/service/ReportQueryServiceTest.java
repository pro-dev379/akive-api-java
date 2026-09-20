package net.akive.api.report.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.report.application.provided.dto.*;
import net.akive.api.report.application.required.ReportRepository;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportCoveragePeriod;
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
import static net.akive.api.report.application.service.fixture.ReportQueryFixture.createWrittenReportHeaders;
import static net.akive.api.report.domain.ReportType.*;
import static net.akive.api.report.domain.fixture.ReportFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.*;
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
    void 리포트를_조회한다() {
        Report report = createReconstructedReport();
        given(reportRepository.findByReportIdAndUserId(REPORT_ID, USER_ID))
                .willReturn(Optional.of(report));

        Report foundReport = reportQueryService.find(REPORT_ID, USER_ID);

        assertThat(foundReport).isEqualTo(report);

        then(reportRepository).should().findByReportIdAndUserId(REPORT_ID, USER_ID);
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
    void 리포트_피드백_히스토리를_조회한다() {
        LocalDate startOn = REPORT_ON.minusDays(1);
        LocalDate endOn = REPORT_ON;
        ReportFeedbackHistoryFindQuery query = new ReportFeedbackHistoryFindQuery(USER_ID, WEEKLY, startOn, endOn);
        List<ReportFeedback> firstFeedbackItems = createReportFeedbackItems();
        List<ReportFeedback> secondFeedbackItems = createReportFeedbackItems();
        Report firstReport = createReconstructedReport(startOn, firstFeedbackItems);
        Report secondReport = createReconstructedReport(endOn, secondFeedbackItems);
        given(reportRepository.findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(
                USER_ID,
                WEEKLY,
                startOn,
                endOn
        )).willReturn(List.of(firstReport, secondReport));

        List<ReportFeedbackHistoryResult> feedbackHistory = reportQueryService.findFeedbackHistory(query);

        assertThat(feedbackHistory)
                .containsExactly(
                        new ReportFeedbackHistoryResult(startOn, firstFeedbackItems),
                        new ReportFeedbackHistoryResult(endOn, secondFeedbackItems)
                );
        then(reportRepository).should()
                .findAllByUserIdAndReportTypeAndReportOnBetweenOrderByReportOnAsc(USER_ID, WEEKLY, startOn, endOn);
    }

    @Test
    void 리포트_커버_기간을_조회한다() {
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(USER_ID, REPORT_ON, MONTHLY);
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        ReportCoveragePeriod coveragePeriod = reportQueryService.findCoveragePeriod(query);

        assertThat(coveragePeriod).isEqualTo(new ReportCoveragePeriod(
                LocalDate.of(2026, 1, 1),
                REPORT_ON
        ));
        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 리포트_날짜와_관련된_세셔널_플랜이_없으면_리포트_커버_기간_조회에_실패한다() {
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(
                USER_ID,
                LocalDate.of(2024, 1, 1),
                MONTHLY
        );
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.findCoveragePeriod(query))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 기준일로_리포트_작성_여부를_조회한다() {
        LocalDate baseDate = LocalDate.of(2026, 1, 20);
        LocalDate startOn = LocalDate.of(2026, 1, 4);
        LocalDate endOn = LocalDate.of(2026, 11, 29);
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));
        given(reportRepository.findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(USER_ID, startOn, endOn))
                .willReturn(createWrittenReportHeaders());

        List<ReportWritingStatusResult> writingStatuses = reportQueryService.findWritingStatuses(USER_ID, baseDate);

        assertWritingStatusesForBaseDate20260120(writingStatuses);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).should().findHeadersByUserIdAndReportOnBetweenOrderByReportOnAsc(
                USER_ID,
                startOn,
                endOn
        );
    }

    @Test
    void 기준일과_관련된_세셔널_플랜이_없으면_리포트_작성_여부_조회에_실패한다() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.findWritingStatuses(USER_ID, baseDate))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
        then(reportRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 리포트_작성_가능한_리포트_타입을_조회한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        List<ReportType> reportTypes = reportQueryService.findAvailableReportTypes(USER_ID, REPORT_ON);

        assertThat(reportTypes).containsExactly(WEEKLY, MONTHLY);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    @Test
    void 리포트_날짜와_관련된_세셔널_플랜이_없으면_리포트_작성_가능한_리포트_타입_조회에_실패한다() {
        given(sessionalPlanFinder.findSessionPeriodsByUserId(USER_ID))
                .willReturn(List.of(createYear2026SessionPeriod()));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> reportQueryService.findAvailableReportTypes(USER_ID, LocalDate.of(2024, 1, 1)))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(SESSIONAL_PLAN_NOT_FOUND);

        then(sessionalPlanFinder).should().findSessionPeriodsByUserId(USER_ID);
    }

    private static void assertWritingStatusesForBaseDate20260120(List<ReportWritingStatusResult> writingStatuses) {
        assertThat(writingStatuses).hasSize(19);
        assertThat(writingStatuses)
                .extracting(
                        ReportWritingStatusResult::reportType,
                        ReportWritingStatusResult::reportOn,
                        ReportWritingStatusResult::isWritten
                )
                .containsExactly(
                        tuple(DAILY, LocalDate.of(2026, 1, 19), false),
                        tuple(DAILY, LocalDate.of(2026, 1, 20), true),
                        tuple(DAILY, LocalDate.of(2026, 1, 21), false),
                        tuple(DAILY, LocalDate.of(2026, 1, 22), false),
                        tuple(DAILY, LocalDate.of(2026, 1, 23), false),
                        tuple(DAILY, LocalDate.of(2026, 1, 24), false),
                        tuple(WEEKLY, LocalDate.of(2026, 1, 4), false),
                        tuple(WEEKLY, LocalDate.of(2026, 1, 11), false),
                        tuple(WEEKLY, LocalDate.of(2026, 1, 18), true),
                        tuple(WEEKLY, LocalDate.of(2026, 1, 25), false),
                        tuple(MONTHLY, LocalDate.of(2026, 1, 25), true),
                        tuple(MONTHLY, LocalDate.of(2026, 2, 22), false),
                        tuple(MONTHLY, LocalDate.of(2026, 3, 22), false),
                        tuple(MONTHLY, LocalDate.of(2026, 4, 19), false),
                        tuple(MONTHLY, LocalDate.of(2026, 5, 17), false),
                        tuple(MONTHLY, LocalDate.of(2026, 6, 14), false),
                        tuple(BIANNUALLY, LocalDate.of(2026, 6, 14), false),
                        tuple(BIANNUALLY, LocalDate.of(2026, 11, 29), false),
                        tuple(ANNUALLY, LocalDate.of(2026, 11, 29), false)
                );
    }
}
