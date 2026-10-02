package net.akive.api.report.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.request.ReportFeedbackUpdateRequest;
import net.akive.api.report.adapter.webapi.fixture.ReportRequestFixture;
import net.akive.api.report.application.provided.ReportCreator;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.application.provided.dto.*;
import net.akive.api.report.domain.Report;
import net.akive.api.report.domain.ReportCoveragePeriod;
import net.akive.api.report.domain.ReportFeedback;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceConflictError.REPORT_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.*;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.report.adapter.webapi.fixture.ReportRequestFixture.*;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportCreateCommand;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportFeedbackItemsUpdateCommand;
import static net.akive.api.report.application.service.fixture.ReportQueryFixture.*;
import static net.akive.api.report.domain.ReportType.MONTHLY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.report.domain.fixture.ReportFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(ReportApi.class)
class ReportApiTest extends PrivateApiTestSupport {

    private static final String REPORTS_URL = "/reports";
    private static final String REPORT_URL = "/reports/{reportId}";
    private static final String REPORT_FEEDBACK_URL = "/reports/{reportId}/feedback";
    private static final String REPORT_HEADERS_URL = "/reports/headers";
    private static final String REPORT_FEEDBACK_HISTORY_URL = "/reports/feedback-history";
    private static final String REPORT_COVERAGE_PERIOD_URL = "/reports/coverage-period";
    private static final String REPORT_WRITING_STATUSES_URL = "/reports/writing-statuses";
    private static final String AVAILABLE_REPORT_TYPES_URL = "/reports/available-types?reportOn={reportOn}";

    @MockitoBean
    ReportFinder reportFinder;

    @MockitoBean
    ReportCreator reportCreator;

    @Test
    void 리포트를_생성한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        given(reportCreator.create(command)).willReturn(REPORT_ID);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.reportId")
                .isEqualTo(REPORT_ID.intValue());

        then(reportCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 리포트_생성_요청값이_올바르지_않으면_400을_응답한다(ReportCreateRequest request) {
        postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(reportCreator).should(never()).create(any(ReportCreateCommand.class));
    }

    @Test
    void 리포트_생성_시_피드백_내용이_하나도_없으면_400을_응답한다() {
        ReportCreateRequest reportCreateRequest = createReportCreateRequest(createEmptyReportFeedbackRequest());

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(reportCreateRequest)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMPTY_COLLECTION);

        then(reportCreator).should(never()).create(any(ReportCreateCommand.class));
    }

    @Test
    void 리포트_생성_시_리포트_작성일이_미래면_400을_응답한다() {
        ReportCreateRequest request = createReportCreateRequest(REQUESTED_AT.toLocalDate().plusDays(1));
        ReportCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(REPORT_DATE_IN_FUTURE))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, REPORT_DATE_IN_FUTURE);

        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_리포트_작성일에_맞지_않는_리포트_타입으로_요청하면_400을_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        willThrow(new BusinessRuleViolationException(INVALID_REPORT_TYPE))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_REPORT_TYPE);

        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_회원이_없으면_404를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(UNKNOWN_USER_ID);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, UNKNOWN_USER_ID, toJson(request)).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);

        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_세셔널_플랜이_없으면_404를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_이미_리포트_작성일과_타입이_같은_리포트가_존재하면_409를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        willThrow(new ResourceConflictException(REPORT_ALREADY_EXISTS))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(CONFLICT);

        assertErrorResponse(result, REPORT_ALREADY_EXISTS);

        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트를_조회한다() {
        Report report = createReconstructedReport();
        given(reportFinder.find(REPORT_ID, USER_ID)).willReturn(report);

        MvcTestResultAssert result = get(REPORT_URL, USER_ID, REPORT_ID).hasStatusOk();

        result.bodyJson()
                .extractingPath("$.reportId")
                .isEqualTo(report.getReportId().intValue());
        result.bodyJson()
                .extractingPath("$.reportOn")
                .isEqualTo(report.getReportOn().toString());
        result.bodyJson()
                .extractingPath("$.reportType")
                .isEqualTo(report.getReportType().name());
        result.bodyJson()
                .extractingPath("$.feedback.highlights[0]")
                .isEqualTo(report.getFeedbackItems().get(0).content());
        result.bodyJson()
                .extractingPath("$.feedback.improvements[0]")
                .isEqualTo(report.getFeedbackItems().get(1).content());

        then(reportFinder).should().find(REPORT_ID, USER_ID);
    }

    @Test
    void 리포트_조회_시_리포트가_없으면_404를_응답한다() {
        given(reportFinder.find(UNKNOWN_REPORT_ID, USER_ID))
                .willThrow(new ResourceNotFoundException(REPORT_NOT_FOUND));

        MvcTestResultAssert result = get(REPORT_URL, USER_ID, UNKNOWN_REPORT_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, REPORT_NOT_FOUND);

        then(reportFinder).should().find(UNKNOWN_REPORT_ID, USER_ID);
    }

    @Test
    void 리포트를_삭제한다() {
        delete(REPORT_URL, USER_ID, REPORT_ID).hasStatus(NO_CONTENT);

        then(reportCreator).should().delete(REPORT_ID, USER_ID);
    }

    @Test
    void 리포트_삭제_시_리포트가_없으면_404를_응답한다() {
        willThrow(new ResourceNotFoundException(REPORT_NOT_FOUND))
                .given(reportCreator)
                .delete(UNKNOWN_REPORT_ID, USER_ID);

        MvcTestResultAssert result = delete(REPORT_URL, USER_ID, UNKNOWN_REPORT_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, REPORT_NOT_FOUND);

        then(reportCreator).should().delete(UNKNOWN_REPORT_ID, USER_ID);
    }

    @Test
    void 리포트_피드백을_수정한다() {
        ReportFeedbackUpdateRequest request = createReportFeedbackUpdateRequest();
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(USER_ID);

        putJson(REPORT_FEEDBACK_URL, USER_ID, toJson(request), REPORT_ID).hasStatus(NO_CONTENT);

        then(reportCreator).should().updateFeedbackItems(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 리포트_피드백_수정_요청값이_올바르지_않으면_400을_응답한다(ReportFeedbackUpdateRequest request) {
        putJson(REPORT_FEEDBACK_URL, USER_ID, toJson(request), REPORT_ID).hasStatus(BAD_REQUEST);

        then(reportCreator).should(never()).updateFeedbackItems(any(ReportFeedbackItemsUpdateCommand.class));
    }

    @Test
    void 리포트_피드백_수정_시_피드백_내용이_하나도_없으면_400을_응답한다() {
        ReportFeedbackUpdateRequest request = new ReportFeedbackUpdateRequest(createEmptyReportFeedbackRequest());

        MvcTestResultAssert result = putJson(REPORT_FEEDBACK_URL, USER_ID, toJson(request), REPORT_ID)
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMPTY_COLLECTION);

        then(reportCreator).should(never()).updateFeedbackItems(any(ReportFeedbackItemsUpdateCommand.class));
    }

    @Test
    void 리포트_피드백_수정_시_리포트가_없으면_404를_응답한다() {
        ReportFeedbackUpdateRequest request = createReportFeedbackUpdateRequest();
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(UNKNOWN_REPORT_ID, USER_ID);
        willThrow(new ResourceNotFoundException(REPORT_NOT_FOUND))
                .given(reportCreator)
                .updateFeedbackItems(command);

        MvcTestResultAssert result = putJson(REPORT_FEEDBACK_URL, USER_ID, toJson(request), UNKNOWN_REPORT_ID)
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, REPORT_NOT_FOUND);

        then(reportCreator).should().updateFeedbackItems(command);
    }

    @Test
    void 리포트_헤더_목록을_리포트_최신_작성일_순으로_페이징_조회한다() {
        ReportHeaderResult reportHeader = new ReportHeaderResult(REPORT_ID, REPORT_ON, WEEKLY);
        PageResult<ReportHeaderResult> reportHeaderPage = createPageResult(
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                List.of(reportHeader)
        );
        given(reportFinder.findHeaderPage(eq(USER_ID), any(PageCondition.class)))
                .willReturn(reportHeaderPage);

        MvcTestResultAssert result = get(
                REPORT_HEADERS_URL + "?page={page}&size={size}",
                USER_ID,
                DEFAULT_PAGE,
                DEFAULT_SIZE
        ).hasStatusOk();

        assertReportHeaderFindResponse(result, reportHeaderPage, reportHeader);

        ArgumentCaptor<PageCondition> pageConditionCaptor = ArgumentCaptor.forClass(PageCondition.class);
        then(reportFinder).should().findHeaderPage(eq(USER_ID), pageConditionCaptor.capture());
        assertThat(pageConditionCaptor.getValue().getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageConditionCaptor.getValue().getSize()).isEqualTo(DEFAULT_SIZE);
        assertThat(pageConditionCaptor.getValue().getSortCondition().property()).isEqualTo("reportOn");
        assertThat(pageConditionCaptor.getValue().getSortCondition().sortDirection()).isEqualTo(DESC);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 7, Page must not be negative.",
            "0, 0, Size must be positive.",
            "0, 101, Size must not exceed 100."
    })
    void 리포트_헤더_목록_조회_시_페이지_요청_파라미터가_올바르지_않으면_400을_응답한다(int page, int size, String message) {
        MvcTestResultAssert result = get(
                REPORT_HEADERS_URL + "?page={page}&size={size}",
                USER_ID,
                page,
                size
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("argument");
        result.bodyJson()
                .extractingPath("$.errors[0].message")
                .isEqualTo(message);

        then(reportFinder).shouldHaveNoInteractions();
    }

    @Test
    void 리포트_피드백_히스토리를_조회한다() {
        LocalDate startOn = REPORT_ON.minusDays(1);
        LocalDate endOn = REPORT_ON;
        ReportFeedbackHistoryFindQuery query = new ReportFeedbackHistoryFindQuery(USER_ID, WEEKLY, startOn, endOn);
        List<ReportFeedback> firstFeedbackItems = createReportFeedbackItems();
        List<ReportFeedback> secondFeedbackItems = createReportFeedbackItems();
        given(reportFinder.findFeedbackHistory(query))
                .willReturn(List.of(
                        new ReportFeedbackHistoryResult(startOn, firstFeedbackItems),
                        new ReportFeedbackHistoryResult(endOn, secondFeedbackItems)
                ));

        MvcTestResultAssert result = get(
                REPORT_FEEDBACK_HISTORY_URL + "?reportType={reportType}&startOn={startOn}&endOn={endOn}",
                USER_ID,
                WEEKLY,
                startOn,
                endOn
        ).hasStatusOk();

        result.bodyJson()
                .extractingPath("$.feedbackHistory['" + startOn + "'].highlights[0]")
                .isEqualTo(firstFeedbackItems.get(0).content());
        result.bodyJson()
                .extractingPath("$.feedbackHistory['" + startOn + "'].improvements[0]")
                .isEqualTo(firstFeedbackItems.get(1).content());
        result.bodyJson()
                .extractingPath("$.feedbackHistory['" + endOn + "'].highlights[0]")
                .isEqualTo(secondFeedbackItems.get(0).content());
        result.bodyJson()
                .extractingPath("$.feedbackHistory['" + endOn + "'].improvements[0]")
                .isEqualTo(secondFeedbackItems.get(1).content());

        then(reportFinder).should().findFeedbackHistory(query);
    }

    @ParameterizedTest
    @CsvSource({
            "INVALID, 2026-01-24, 2026-01-25",
            "WEEKLY, invalid-date, 2026-01-25",
            "WEEKLY, 2026-01-24, invalid-date"
    })
    void 리포트_피드백_히스토리_조회_요청_파라미터가_올바르지_않으면_400을_응답한다(
            String reportType,
            String startOn,
            String endOn
    ) {
        get(
            REPORT_FEEDBACK_HISTORY_URL + "?reportType={reportType}&startOn={startOn}&endOn={endOn}",
            USER_ID,
            reportType,
            startOn,
            endOn
        ).hasStatus(BAD_REQUEST);

        then(reportFinder).shouldHaveNoInteractions();
    }

    @Test
    void 리포트_피드백_히스토리_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                REPORT_FEEDBACK_HISTORY_URL + "?reportType={reportType}&startOn={startOn}&endOn={endOn}",
                USER_ID,
                WEEKLY,
                REPORT_ON.plusDays(1),
                REPORT_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(reportFinder).shouldHaveNoInteractions();
    }

    @Test
    void 리포트_커버_기간을_조회한다() {
        ReportCoveragePeriod coveragePeriod = new ReportCoveragePeriod(LocalDate.of(2026, 1, 1), REPORT_ON);
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(USER_ID, REPORT_ON, MONTHLY);
        given(reportFinder.findCoveragePeriod(query))
                .willReturn(coveragePeriod);

        MvcTestResultAssert result = get(
                REPORT_COVERAGE_PERIOD_URL + "?reportOn={reportOn}&reportType={reportType}",
                USER_ID,
                REPORT_ON,
                MONTHLY
        ).hasStatusOk();

        result.bodyJson()
                .extractingPath("$.startOn")
                .isEqualTo(coveragePeriod.startOn().toString());
        result.bodyJson()
                .extractingPath("$.endOn")
                .isEqualTo(coveragePeriod.endOn().toString());

        then(reportFinder).should().findCoveragePeriod(query);
    }

    @Test
    void 리포트_커버_기간_조회_시_리포트_작성일에_맞지_않는_리포트_타입으로_요청하면_400을_응답한다() {
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(USER_ID, REPORT_ON, MONTHLY);
        given(reportFinder.findCoveragePeriod(query))
                .willThrow(new BusinessRuleViolationException(INVALID_REPORT_TYPE));

        MvcTestResultAssert result = get(
                REPORT_COVERAGE_PERIOD_URL + "?reportOn={reportOn}&reportType={reportType}",
                USER_ID,
                REPORT_ON,
                MONTHLY
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_REPORT_TYPE);

        then(reportFinder).should().findCoveragePeriod(query);
    }

    @Test
    void 리포트_커버_기간_조회_시_리포트_작성일과_관련된_세셔널_플랜이_없으면_404를_응답한다() {
        ReportCoveragePeriodFindQuery query = new ReportCoveragePeriodFindQuery(USER_ID, REPORT_ON, MONTHLY);
        given(reportFinder.findCoveragePeriod(query))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(
                REPORT_COVERAGE_PERIOD_URL + "?reportOn={reportOn}&reportType={reportType}",
                USER_ID,
                REPORT_ON,
                MONTHLY
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(reportFinder).should().findCoveragePeriod(query);
    }

    @ParameterizedTest
    @CsvSource({
            "invalid-date, MONTHLY",
            "2026-01-25, INVALID"
    })
    void 리포트_커버_기간_조회_요청_파라미터가_올바르지_않으면_400을_응답한다(String reportOn, String reportType) {
        get(
            REPORT_COVERAGE_PERIOD_URL + "?reportOn={reportOn}&reportType={reportType}",
            USER_ID,
            reportOn,
            reportType
        ).hasStatus(BAD_REQUEST);

        then(reportFinder).shouldHaveNoInteractions();
    }

    @Test
    void 오늘_기준으로_리포트_작성_여부를_조회한다() {
        ReportWritingStatusResult dailyWritingStatus = createDailyWritingStatus();
        ReportWritingStatusResult weeklyWritingStatus = createWeeklyWritingStatus();
        ReportWritingStatusResult monthlyWritingStatus = createMonthlyWritingStatus();
        given(reportFinder.findWritingStatuses(USER_ID))
                .willReturn(List.of(dailyWritingStatus, weeklyWritingStatus, monthlyWritingStatus));

        MvcTestResultAssert result = get(REPORT_WRITING_STATUSES_URL, USER_ID).hasStatusOk();

        assertReportWritingStatusesFindResponse(
                result,
                dailyWritingStatus,
                weeklyWritingStatus,
                monthlyWritingStatus
        );

        then(reportFinder).should().findWritingStatuses(USER_ID);
    }

    @Test
    void 오늘_기준으로_리포트_작성_여부를_조회_시_오늘과_관련된_세셔널_플랜이_없으면_404를_응답한다() {
        given(reportFinder.findWritingStatuses(USER_ID))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(REPORT_WRITING_STATUSES_URL, USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(reportFinder).should().findWritingStatuses(USER_ID);
    }

    @Test
    void 리포트_작성_가능한_리포트_타입을_조회한다() {
        given(reportFinder.findAvailableReportTypes(USER_ID, REPORT_ON))
                .willReturn(List.of(WEEKLY, MONTHLY));

        MvcTestResultAssert result = get(AVAILABLE_REPORT_TYPES_URL, USER_ID, REPORT_ON).hasStatusOk();

        result.bodyJson()
                .extractingPath("$.reportTypes[0].code")
                .isEqualTo(WEEKLY.name());
        result.bodyJson()
                .extractingPath("$.reportTypes[0].label")
                .isEqualTo(WEEKLY.getLabel());
        result.bodyJson()
                .extractingPath("$.reportTypes[1].code")
                .isEqualTo(MONTHLY.name());
        result.bodyJson()
                .extractingPath("$.reportTypes[1].label")
                .isEqualTo(MONTHLY.getLabel());

        then(reportFinder).should().findAvailableReportTypes(USER_ID, REPORT_ON);
    }

    @Test
    void 리포트_작성_가능한_리포트_타입_조회_시_리포트_작성일과_관련된_세셔널_플랜이_없으면_404를_응답한다() {
        given(reportFinder.findAvailableReportTypes(USER_ID, REPORT_ON))
                .willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));

        MvcTestResultAssert result = get(AVAILABLE_REPORT_TYPES_URL, USER_ID, REPORT_ON).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(reportFinder).should().findAvailableReportTypes(USER_ID, REPORT_ON);
    }

    private static Stream<ReportCreateRequest> invalidCreateRequests() {
        return ReportRequestFixture.invalidCreateRequests();
    }

    private static Stream<ReportFeedbackUpdateRequest> invalidUpdateRequests() {
        return ReportRequestFixture.invalidUpdateRequests();
    }

    private static void assertReportHeaderFindResponse(
            MvcTestResultAssert result,
            PageResult<ReportHeaderResult> reportHeaderPage,
            ReportHeaderResult reportHeader
    ) {
        result.bodyJson().extractingPath("$.pageInfo.page")
                .isEqualTo(reportHeaderPage.pageInfo().page());
        result.bodyJson().extractingPath("$.pageInfo.size")
                .isEqualTo(reportHeaderPage.pageInfo().size());
        result.bodyJson().extractingPath("$.pageInfo.totalElement")
                .isEqualTo((int) reportHeaderPage.pageInfo().totalElement());
        result.bodyJson().extractingPath("$.pageInfo.totalPage")
                .isEqualTo(reportHeaderPage.pageInfo().totalPage());
        result.bodyJson().extractingPath("$.pageInfo.first")
                .isEqualTo(reportHeaderPage.pageInfo().first());
        result.bodyJson().extractingPath("$.pageInfo.last")
                .isEqualTo(reportHeaderPage.pageInfo().last());
        result.bodyJson().extractingPath("$.reportHeaders[0].reportId")
                .isEqualTo(reportHeader.reportId().intValue());
        result.bodyJson().extractingPath("$.reportHeaders[0].reportOn")
                .isEqualTo(reportHeader.reportOn().toString());
        result.bodyJson().extractingPath("$.reportHeaders[0].reportType")
                .isEqualTo(reportHeader.reportType().name());
    }

    private static void assertReportWritingStatusesFindResponse(
            MvcTestResultAssert result,
            ReportWritingStatusResult dailyWritingStatus,
            ReportWritingStatusResult weeklyWritingStatus,
            ReportWritingStatusResult monthlyWritingStatus
    ) {
        result.bodyJson()
                .extractingPath("$.dailyReports[0].reportOn")
                .isEqualTo(dailyWritingStatus.reportOn().toString());
        result.bodyJson()
                .extractingPath("$.dailyReports[0].isWritten")
                .isEqualTo(dailyWritingStatus.isWritten());
        result.bodyJson()
                .extractingPath("$.weeklyReports[0].reportOn")
                .isEqualTo(weeklyWritingStatus.reportOn().toString());
        result.bodyJson()
                .extractingPath("$.weeklyReports[0].isWritten")
                .isEqualTo(weeklyWritingStatus.isWritten());
        result.bodyJson()
                .extractingPath("$.monthlyReports[0].reportOn")
                .isEqualTo(monthlyWritingStatus.reportOn().toString());
        result.bodyJson()
                .extractingPath("$.monthlyReports[0].isWritten")
                .isEqualTo(monthlyWritingStatus.isWritten());
        result.bodyJson().extractingPath("$.biannualReports").asArray().isEmpty();
        result.bodyJson().extractingPath("$.annualReports").asArray().isEmpty();
    }
}
