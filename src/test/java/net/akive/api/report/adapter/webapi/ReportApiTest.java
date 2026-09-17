package net.akive.api.report.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.report.adapter.webapi.dto.request.ReportCreateRequest;
import net.akive.api.report.adapter.webapi.dto.request.ReportFeedbackUpdateRequest;
import net.akive.api.report.adapter.webapi.fixture.ReportRequestFixture;
import net.akive.api.report.application.provided.ReportCreator;
import net.akive.api.report.application.provided.ReportFinder;
import net.akive.api.report.application.provided.dto.ReportCreateCommand;
import net.akive.api.report.application.provided.dto.ReportFeedbackItemsUpdateCommand;
import net.akive.api.report.domain.Report;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceConflictError.REPORT_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.*;
import static net.akive.api.report.adapter.webapi.fixture.ReportRequestFixture.*;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportCreateCommand;
import static net.akive.api.report.application.service.fixture.ReportCommandFixture.createReportFeedbackItemsUpdateCommand;
import static net.akive.api.report.domain.ReportFixture.*;
import static net.akive.api.report.domain.ReportType.MONTHLY;
import static net.akive.api.report.domain.ReportType.WEEKLY;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(ReportApi.class)
class ReportApiTest extends PrivateApiTestSupport {

    private static final String REPORTS_URL = "/reports";
    private static final String REPORT_URL = "/reports/{reportId}";
    private static final String AVAILABLE_REPORT_TYPES_URL = "/reports/available-types?reportOn={reportOn}";

    @MockitoBean
    ReportFinder reportFinder;

    @MockitoBean
    ReportCreator reportCreator;

    @MockitoBean
    CurrentTimeProvider currentTimeProvider;

    @Test
    void 리포트를_생성한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        given(reportCreator.create(command)).willReturn(REPORT_ID);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.reportId")
                .isEqualTo(REPORT_ID.intValue());

        then(currentTimeProvider).should().now();
        then(reportCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 리포트_생성_요청값이_올바르지_않으면_400을_응답한다(ReportCreateRequest request) {
        postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(currentTimeProvider).should(never()).now();
        then(reportCreator).should(never()).create(any(ReportCreateCommand.class));
    }

    @Test
    void 리포트_생성_시_피드백_내용이_하나도_없으면_400을_응답한다() {
        ReportCreateRequest reportCreateRequest = createReportCreateRequest(createEmptyReportFeedbackRequest());
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(reportCreateRequest)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMPTY_COLLECTION);

        then(currentTimeProvider).should().now();
        then(reportCreator).should(never()).create(any(ReportCreateCommand.class));
    }

    @Test
    void 리포트_생성_시_리포트_작성일이_미래면_400을_응답한다() {
        ReportCreateRequest request = createReportCreateRequest(REQUESTED_AT.toLocalDate().plusDays(1));
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, REPORT_DATE_IN_FUTURE);

        then(currentTimeProvider).should().now();
        then(reportCreator).should(never()).create(any(ReportCreateCommand.class));
    }

    @Test
    void 리포트_생성_시_작성할_수_없는_리포트_타입이면_400을_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        willThrow(new BusinessRuleViolationException(INVALID_REPORT_TYPE))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_REPORT_TYPE);

        then(currentTimeProvider).should().now();
        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_회원이_없으면_404를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(UNKNOWN_USER_ID);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, UNKNOWN_USER_ID, toJson(request)).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);

        then(currentTimeProvider).should().now();
        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_세셔널_플랜이_없으면_404를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        willThrow(new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(NOT_FOUND);

        assertErrorResponse(result, SESSIONAL_PLAN_NOT_FOUND);

        then(currentTimeProvider).should().now();
        then(reportCreator).should().create(command);
    }

    @Test
    void 리포트_생성_시_이미_리포트_작성일과_리포트_타입이_같은_리포트가_존재하면_409를_응답한다() {
        ReportCreateRequest request = createReportCreateRequest();
        ReportCreateCommand command = createReportCreateCommand(USER_ID);
        given(currentTimeProvider.now()).willReturn(REQUESTED_AT);
        willThrow(new ResourceConflictException(REPORT_ALREADY_EXISTS))
                .given(reportCreator)
                .create(command);

        MvcTestResultAssert result = postJson(REPORTS_URL, USER_ID, toJson(request)).hasStatus(CONFLICT);

        assertErrorResponse(result, REPORT_ALREADY_EXISTS);

        then(currentTimeProvider).should().now();
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
    void 리포트_피드백_목록을_수정한다() {
        ReportFeedbackUpdateRequest request = createReportFeedbackUpdateRequest();
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(USER_ID);

        putJson(REPORT_URL, USER_ID, toJson(request), REPORT_ID).hasStatus(NO_CONTENT);

        then(reportCreator).should().updateFeedbackItems(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 리포트_수정_요청값이_올바르지_않으면_400을_응답한다(ReportFeedbackUpdateRequest request) {
        putJson(REPORT_URL, USER_ID, toJson(request), REPORT_ID).hasStatus(BAD_REQUEST);

        then(reportCreator).should(never()).updateFeedbackItems(any(ReportFeedbackItemsUpdateCommand.class));
    }

    @Test
    void 리포트_수정_시_피드백_내용이_하나도_없으면_400을_응답한다() {
        ReportFeedbackUpdateRequest request = new ReportFeedbackUpdateRequest(createEmptyReportFeedbackRequest());

        MvcTestResultAssert result = putJson(REPORT_URL, USER_ID, toJson(request), REPORT_ID)
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMPTY_COLLECTION);

        then(reportCreator).should(never()).updateFeedbackItems(any(ReportFeedbackItemsUpdateCommand.class));
    }

    @Test
    void 리포트_수정_시_리포트가_없으면_404를_응답한다() {
        ReportFeedbackUpdateRequest request = createReportFeedbackUpdateRequest();
        ReportFeedbackItemsUpdateCommand command = createReportFeedbackItemsUpdateCommand(UNKNOWN_REPORT_ID, USER_ID);
        willThrow(new ResourceNotFoundException(REPORT_NOT_FOUND))
                .given(reportCreator)
                .updateFeedbackItems(command);

        MvcTestResultAssert result = putJson(REPORT_URL, USER_ID, toJson(request), UNKNOWN_REPORT_ID)
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, REPORT_NOT_FOUND);

        then(reportCreator).should().updateFeedbackItems(command);
    }

    @Test
    void 작성_가능한_리포트_타입을_조회한다() {
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
    void 작성_가능한_리포트_타입_조회_시_리포트_날짜와_관련된_세셔널_플랜이_없으면_404를_응답한다() {
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
}
