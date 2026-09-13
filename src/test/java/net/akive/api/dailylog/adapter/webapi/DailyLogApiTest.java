package net.akive.api.dailylog.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.fixture.DailyLogRequestFixture;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
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

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceConflictError.DAILY_LOG_TIME_OVERLAPS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.dailylog.adapter.webapi.fixture.DailyLogRequestFixture.createDailyLogCreateRequest;
import static net.akive.api.dailylog.adapter.webapi.fixture.DailyLogRequestFixture.createDailyLogTimeRangeRequest;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(DailyLogApi.class)
class DailyLogApiTest extends PrivateApiTestSupport {

    private static final String DAILY_LOGS_URL = "/daily-logs/{logOn}";
    private static final String DAILY_LOG_ONS_URL = "/daily-logs/log-ons";

    @MockitoBean
    DailyLogFinder dailyLogFinder;

    @MockitoBean
    DailyLogCreator dailyLogCreator;

    @MockitoBean
    CurrentTimeProvider currentTimeProvider;

    @Test
    void 일일_로그를_생성한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID, LOG_ON, NOW);

        given(currentTimeProvider.now()).willReturn(NOW);
        given(dailyLogCreator.create(command)).willReturn(DAILY_LOG_ID);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.dailyLogId")
                .isEqualTo(DAILY_LOG_ID.intValue());

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 일일_로그_생성_요청값이_올바르지_않으면_400을_응답한다(DailyLogCreateRequest request) {
        postJson(DAILY_LOGS_URL, USER_ID, toJson(request), LOG_ON).hasStatus(BAD_REQUEST);

        then(currentTimeProvider).shouldHaveNoInteractions();
        then(dailyLogCreator).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그_생성_시_미래_시간_범위로_요청하면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createDailyLogTimeRangeRequest(
                        NOW.plusHours(1),
                        NOW.plusHours(2)
                )
        );

        given(currentTimeProvider.now()).willReturn(NOW);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_IN_FUTURE);

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그_생성_시_시간_범위가_24시간을_초과하면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createDailyLogTimeRangeRequest(
                        startOn(LOG_ON, 9),
                        endOn(LOG_ON.plusDays(1), 10)
                )
        );

        given(currentTimeProvider.now()).willReturn(NOW);


        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_TOO_LONG);

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그_생성_시_집중_시간이_시간_범위보다_크면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createDailyLogTimeRangeRequest(startOn(LOG_ON, 9), endOn(LOG_ON, 10)),
                3601L
        );
        DailyLogCreateCommand command = request.to(USER_ID, LOG_ON, NOW);

        given(currentTimeProvider.now()).willReturn(NOW);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_회원이_없으면_404를_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID, LOG_ON, NOW);

        given(currentTimeProvider.now()).willReturn(NOW);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_기존_일일_로그의_시간_범위와_오버랩되면_409를_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID, LOG_ON, NOW);

        given(currentTimeProvider.now()).willReturn(NOW);
        willThrow(new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request),
                LOG_ON
        ).hasStatus(CONFLICT);

        assertErrorResponse(result, DAILY_LOG_TIME_OVERLAPS);

        then(currentTimeProvider).should().now();
        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 특정_날짜의_일일_로그_목록을_조회한다() {
        User user = createReconstructedUser(USER_ID);
        DailyLog dailyLog = createReconstructedDailyLog(user);

        given(dailyLogFinder.findAll(USER_ID, LOG_ON)).willReturn(List.of(dailyLog));

        MvcTestResultAssert result = get(DAILY_LOGS_URL, USER_ID, LOG_ON).hasStatusOk();

        assertDailyLogResponse(result, dailyLog);

        then(dailyLogFinder).should().findAll(USER_ID, LOG_ON);
    }

    @Test
    void 특정_날짜의_일일_로그_목록_조회_시_요청_날짜가_올바르지_않으면_400을_응답한다() {
        MvcTestResultAssert result = get(DAILY_LOGS_URL, USER_ID, "invalid-date")
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("logOn");
        result.bodyJson()
                .extractingPath("$.errors[0].message")
                .isEqualTo("Invalid date format. Expected yyyy-MM-dd.");

        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그가_있는_날짜_목록을_최신순으로_페이징_조회한다() {
        LocalDate olderLogOn = LocalDate.of(2026, 9, 1);
        LocalDate recentLogOn = olderLogOn.plusDays(1);
        List<LocalDate> contents = List.of(recentLogOn, olderLogOn);
        PageResult<LocalDate> logOns = createPageResult(DEFAULT_PAGE, DEFAULT_SIZE, contents);

        given(dailyLogFinder.findLogOns(eq(USER_ID), any(PageCondition.class))).willReturn(logOns);

        MvcTestResultAssert result = get(
                DAILY_LOG_ONS_URL + "?page={page}&size={size}",
                USER_ID,
                DEFAULT_PAGE,
                DEFAULT_SIZE
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.page").isEqualTo(DEFAULT_PAGE);
        result.bodyJson().extractingPath("$.size").isEqualTo(DEFAULT_SIZE);
        result.bodyJson().extractingPath("$.totalElement").isEqualTo(contents.size());
        result.bodyJson().extractingPath("$.totalPage").isEqualTo(1);
        result.bodyJson().extractingPath("$.first").isEqualTo(true);
        result.bodyJson().extractingPath("$.last").isEqualTo(true);
        result.bodyJson().extractingPath("$.contents[0]").isEqualTo(recentLogOn.toString());
        result.bodyJson().extractingPath("$.contents[1]").isEqualTo(olderLogOn.toString());

        ArgumentCaptor<PageCondition> pageConditionCaptor = ArgumentCaptor.forClass(PageCondition.class);
        then(dailyLogFinder).should().findLogOns(eq(USER_ID), pageConditionCaptor.capture());
        assertThat(pageConditionCaptor.getValue().getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageConditionCaptor.getValue().getSize()).isEqualTo(DEFAULT_SIZE);
        assertThat(pageConditionCaptor.getValue().getSortCondition().property()).isEqualTo("logOn");
        assertThat(pageConditionCaptor.getValue().getSortCondition().sortDirection()).isEqualTo(DESC);
    }

    @Test
    void 일일_로그_날짜_목록_조회_시_페이지_파라미터가_없으면_기본값으로_일일_로그가_있는_날짜_목록을_조회한다() {
        PageResult<LocalDate> logOns = createPageResult(List.of(LOG_ON));

        given(dailyLogFinder.findLogOns(eq(USER_ID), any(PageCondition.class))).willReturn(logOns);

        get(DAILY_LOG_ONS_URL, USER_ID).hasStatusOk();

        ArgumentCaptor<PageCondition> pageConditionCaptor = ArgumentCaptor.forClass(PageCondition.class);
        then(dailyLogFinder).should().findLogOns(eq(USER_ID), pageConditionCaptor.capture());
        assertThat(pageConditionCaptor.getValue().getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageConditionCaptor.getValue().getSize()).isEqualTo(DEFAULT_SIZE);
        assertThat(pageConditionCaptor.getValue().getSortCondition().property()).isEqualTo("logOn");
        assertThat(pageConditionCaptor.getValue().getSortCondition().sortDirection()).isEqualTo(DESC);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 7, Page must not be negative.",
            "0, 0, Size must be positive.",
            "0, 101, Size must not exceed 100."
    })
    void 일일_로그_날짜_목록_조회_시_페이지_요청_파라미터가_올바르지_않으면_400을_응답한다(int page, int size, String message) {
        MvcTestResultAssert result = get(
                DAILY_LOG_ONS_URL + "?page={page}&size={size}",
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

        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    private void assertDailyLogResponse(MvcTestResultAssert result, DailyLog dailyLog) {
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].dailyLogId")
                .isEqualTo(dailyLog.getDailyLogId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].timeRange.startOn")
                .isEqualTo(dailyLog.getStartOn().format(ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].timeRange.endOn")
                .isEqualTo(dailyLog.getEndOn().format(ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].dailyLogType")
                .isEqualTo(dailyLog.getDailyLogType().name());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].focusTimeSec")
                .isEqualTo(dailyLog.getFocusTimeSec().intValue());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].contents")
                .isEqualTo(dailyLog.getContents());
    }

    private static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return DailyLogRequestFixture.invalidCreateRequests();
    }
}
