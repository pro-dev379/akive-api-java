package net.akive.api.dailylog.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.cache.ResponseCache;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogCreateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.DailyLogUpdateRequest;
import net.akive.api.dailylog.adapter.webapi.dto.request.ActivityTimeRequest;
import net.akive.api.dailylog.adapter.webapi.fixture.DailyLogRequestFixture;
import net.akive.api.dailylog.application.provided.DailyLogCreator;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogDeleteCommand;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;
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
import static net.akive.api.common.exception.business.ResourceNotFoundError.DAILY_LOG_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.dailylog.adapter.webapi.fixture.DailyLogRequestFixture.*;
import static net.akive.api.dailylog.domain.DailyLogType.*;
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

    private static final String DAILY_LOGS_URL = "/daily-logs";
    private static final String DAILY_LOG_URL = "/daily-logs/{dailyLogId}";
    private static final String DAILY_LOG_ONS_URL = "/daily-logs/log-ons";

    @MockitoBean
    DailyLogFinder dailyLogFinder;

    @MockitoBean
    DailyLogCreator dailyLogCreator;

    @MockitoBean
    ResponseCache responseCache;

    @Test
    void 일일_로그를_생성한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID);

        given(dailyLogCreator.create(command)).willReturn(DAILY_LOG_ID);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(CREATED);

        result.bodyJson()
                .extractingPath("$.dailyLogId")
                .isEqualTo(DAILY_LOG_ID.intValue());

        then(dailyLogCreator).should().create(command);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateRequests")
    void 일일_로그_생성_요청값이_올바르지_않으면_400을_응답한다(DailyLogCreateRequest request) {
        postJson(DAILY_LOGS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(dailyLogCreator).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그_생성_시_미래_시간_범위로_요청하면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createActivityTimeRequest(
                        CURRENT_KOREAN_DATE_TIME.plusHours(1),
                        CURRENT_KOREAN_DATE_TIME.plusHours(2)
                )
        );
        DailyLogCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_IN_FUTURE);

        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_시간_범위가_24시간을_초과하면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createActivityTimeRequest(
                        startOn(LOG_ON, 9),
                        endOn(LOG_ON.plusDays(1), 10)
                )
        );
        DailyLogCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_TOO_LONG))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_TOO_LONG);

        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_실제_집중_시간이_시간_범위보다_크면_400을_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest(
                createActivityTimeRequest(startOn(LOG_ON, 9), endOn(LOG_ON, 10)),
                3601L
        );
        DailyLogCreateCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);

        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_수면_시간이_2시간_미만이면_400을_응답한다() {
        DailyLogCreateRequest request = new DailyLogCreateRequest(
                new ActivityTimeRequest(START_ON, START_ON.plusHours(1), null, false),
                SLEEP,
                "수면"
        );
        DailyLogCreateCommand command = request.to(USER_ID);

        willThrow(new BusinessRuleViolationException(DAILY_LOG_SLEEP_TIME_TOO_SHORT))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(DAILY_LOGS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_SLEEP_TIME_TOO_SHORT);
        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 비학습_일일_로그_생성_시_집중_시간이_있으면_400을_응답한다() {
        DailyLogCreateRequest request = new DailyLogCreateRequest(
                new ActivityTimeRequest(START_ON, END_ON, FOCUS_TIME_SEC, false),
                REST,
                "휴식"
        );
        DailyLogCreateCommand command = request.to(USER_ID);

        willThrow(new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(DAILY_LOGS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_회원이_없으면_404를_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID);

        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);

        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그_생성_시_기존_일일_로그의_시간_범위와_오버랩되면_409를_응답한다() {
        DailyLogCreateRequest request = createDailyLogCreateRequest();
        DailyLogCreateCommand command = request.to(USER_ID);

        willThrow(new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS))
                .given(dailyLogCreator)
                .create(command);

        MvcTestResultAssert result = postJson(
                DAILY_LOGS_URL,
                USER_ID,
                toJson(request)
        ).hasStatus(CONFLICT);

        assertErrorResponse(result, DAILY_LOG_TIME_OVERLAPS);

        then(dailyLogCreator).should().create(command);
    }

    @Test
    void 일일_로그를_수정한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest();
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);


        putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                DAILY_LOG_ID
        ).hasStatus(NO_CONTENT);

        then(dailyLogCreator).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateRequests")
    void 일일_로그_수정_요청값이_올바르지_않으면_400을_응답한다(DailyLogUpdateRequest request) {
        putJson(DAILY_LOG_URL, USER_ID, toJson(request), DAILY_LOG_ID).hasStatus(BAD_REQUEST);

        then(dailyLogCreator).should(never()).update(any(DailyLogUpdateCommand.class));
    }

    @Test
    void 일일_로그_수정_시_미래_시간_범위로_요청하면_400을_응답한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest(
                createActivityTimeRequest(
                        CURRENT_KOREAN_DATE_TIME.plusHours(1),
                        CURRENT_KOREAN_DATE_TIME.plusHours(2)
                )
        );
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_IN_FUTURE))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                DAILY_LOG_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_IN_FUTURE);

        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_시간_범위가_24시간을_초과하면_400을_응답한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest(
                createActivityTimeRequest(
                        startOn(LOG_ON, 9),
                        endOn(LOG_ON.plusDays(1), 10)
                )
        );
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_TIME_RANGE_TOO_LONG))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                DAILY_LOG_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_TIME_RANGE_TOO_LONG);

        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_실제_집중_시간이_시간_범위보다_크면_400을_응답한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest(
                createActivityTimeRequest(startOn(LOG_ON, 9), endOn(LOG_ON, 10)),
                3601L
        );
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);
        willThrow(new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                DAILY_LOG_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_FOCUS_TIME_EXCEEDS_DURATION);

        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_수면_시간이_2시간_미만이면_400을_응답한다() {
        DailyLogUpdateRequest request = new DailyLogUpdateRequest(
                new ActivityTimeRequest(TARGET_START_ON, TARGET_START_ON.plusHours(1), null, false),
                SLEEP,
                "수면"
        );
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);

        willThrow(new BusinessRuleViolationException(DAILY_LOG_SLEEP_TIME_TOO_SHORT))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL, USER_ID, toJson(request), DAILY_LOG_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_SLEEP_TIME_TOO_SHORT);
        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_비학습_로그에_집중_시간이_있으면_400을_응답한다() {
        DailyLogUpdateRequest request = new DailyLogUpdateRequest(
                new ActivityTimeRequest(TARGET_START_ON, TARGET_END_ON, FOCUS_TIME_SEC, false),
                REST,
                "휴식"
        );
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);

        willThrow(new BusinessRuleViolationException(DAILY_LOG_FOCUS_TIME_NOT_ALLOWED))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL, USER_ID, toJson(request), DAILY_LOG_ID
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, DAILY_LOG_FOCUS_TIME_NOT_ALLOWED);
        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_일일_로그가_없으면_404를_응답한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest();
        DailyLogUpdateCommand command = request.to(UNKNOWN_DAILY_LOG_ID, USER_ID);

        willThrow(new ResourceNotFoundException(DAILY_LOG_NOT_FOUND))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                UNKNOWN_DAILY_LOG_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, DAILY_LOG_NOT_FOUND);

        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그_수정_시_시간_범위가_오버랩되면_409를_응답한다() {
        DailyLogUpdateRequest request = createDailyLogUpdateRequest();
        DailyLogUpdateCommand command = request.to(DAILY_LOG_ID, USER_ID);

        willThrow(new ResourceConflictException(DAILY_LOG_TIME_OVERLAPS))
                .given(dailyLogCreator)
                .update(command);

        MvcTestResultAssert result = putJson(
                DAILY_LOG_URL,
                USER_ID,
                toJson(request),
                DAILY_LOG_ID
        ).hasStatus(CONFLICT);

        assertErrorResponse(result, DAILY_LOG_TIME_OVERLAPS);

        then(dailyLogCreator).should().update(command);
    }

    @Test
    void 일일_로그를_삭제한다() {
        DailyLogDeleteCommand command = new DailyLogDeleteCommand(DAILY_LOG_ID, USER_ID);

        delete(DAILY_LOG_URL, USER_ID, DAILY_LOG_ID).hasStatus(NO_CONTENT);

        then(dailyLogCreator).should().delete(command);
    }

    @Test
    void 삭제할_일일_로그가_없으면_404를_응답한다() {
        DailyLogDeleteCommand command = new DailyLogDeleteCommand(UNKNOWN_DAILY_LOG_ID, USER_ID);
        willThrow(new ResourceNotFoundException(DAILY_LOG_NOT_FOUND))
                .given(dailyLogCreator)
                .delete(command);

        MvcTestResultAssert result = delete(DAILY_LOG_URL, USER_ID, UNKNOWN_DAILY_LOG_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, DAILY_LOG_NOT_FOUND);

        then(dailyLogCreator).should().delete(command);
    }

    @Test
    void 특정_날짜의_일일_로그_목록을_조회한다() {
        User user = createReconstructedUser(USER_ID);
        DailyLog dailyLog = createReconstructedDailyLog(user);

        given(dailyLogFinder.findAll(USER_ID, LOG_ON)).willReturn(List.of(dailyLog));

        MvcTestResultAssert result = get(DAILY_LOGS_URL + "?logOn={logOn}", USER_ID, LOG_ON).hasStatusOk();

        assertDailyLogResponse(result, dailyLog);

        then(dailyLogFinder).should().findAll(USER_ID, LOG_ON);
    }

    @Test
    void 특정_날짜의_일일_로그_목록_조회_시_요청_날짜가_올바르지_않으면_400을_응답한다() {
        MvcTestResultAssert result = get(DAILY_LOGS_URL + "?logOn={logOn}", USER_ID, "invalid-date")
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
    void 일일_로그가_있는_날짜_목록을_최신_날짜_순으로_페이징_조회한다() {
        LocalDate olderLogOn = LocalDate.of(2026, 9, 1);
        LocalDate recentLogOn = olderLogOn.plusDays(1);
        List<LocalDate> contents = List.of(recentLogOn, olderLogOn);
        PageResult<LocalDate> logOnPage = createPageResult(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, contents);

        given(dailyLogFinder.findLogOnPage(eq(USER_ID), any(PageCondition.class))).willReturn(logOnPage);

        MvcTestResultAssert result = get(
                DAILY_LOG_ONS_URL + "?page={page}&size={size}",
                USER_ID,
                DEFAULT_PAGE,
                DEFAULT_PAGE_SIZE
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.pageInfo.page").isEqualTo(DEFAULT_PAGE);
        result.bodyJson().extractingPath("$.pageInfo.size").isEqualTo(DEFAULT_PAGE_SIZE);
        result.bodyJson().extractingPath("$.pageInfo.totalElement").isEqualTo(contents.size());
        result.bodyJson().extractingPath("$.pageInfo.totalPage").isEqualTo(1);
        result.bodyJson().extractingPath("$.pageInfo.first").isEqualTo(true);
        result.bodyJson().extractingPath("$.pageInfo.last").isEqualTo(true);
        result.bodyJson().extractingPath("$.logOns[0]").isEqualTo(recentLogOn.toString());
        result.bodyJson().extractingPath("$.logOns[1]").isEqualTo(olderLogOn.toString());

        ArgumentCaptor<PageCondition> pageConditionCaptor = ArgumentCaptor.forClass(PageCondition.class);
        then(dailyLogFinder).should().findLogOnPage(eq(USER_ID), pageConditionCaptor.capture());
        assertThat(pageConditionCaptor.getValue().getPage()).isEqualTo(DEFAULT_PAGE);
        assertThat(pageConditionCaptor.getValue().getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
        assertThat(pageConditionCaptor.getValue().getSortCondition().property()).isEqualTo("logOn");
        assertThat(pageConditionCaptor.getValue().getSortCondition().sortDirection()).isEqualTo(DESC);
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 7, Page must not be negative.",
            "0, 0, Size must be positive.",
            "0, 101, Size must not exceed 100."
    })
    void 일일_로그_날짜_목록_조회_시_페이징_요청_파라미터가_올바르지_않으면_400을_응답한다(int page, int size, String message) {
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

    @Test
    void 일일_로그_타입_목록을_조회한다() {
        given(dailyLogFinder.findDailyLogTypes()).willReturn(List.of(STUDY, SLEEP, REST, MEAL, ATTENDANCE, ETC));

        MvcTestResultAssert result = get("/daily-logs/types", USER_ID).hasStatusOk();

        result.bodyJson().extractingPath("$.dailyLogTypes[0].code").isEqualTo(STUDY.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[0].label").isEqualTo(STUDY.getLabel());
        result.bodyJson().extractingPath("$.dailyLogTypes[1].code").isEqualTo(SLEEP.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[1].label").isEqualTo(SLEEP.getLabel());
        result.bodyJson().extractingPath("$.dailyLogTypes[2].code").isEqualTo(REST.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[2].label").isEqualTo(REST.getLabel());
        result.bodyJson().extractingPath("$.dailyLogTypes[3].code").isEqualTo(MEAL.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[3].label").isEqualTo(MEAL.getLabel());
        result.bodyJson().extractingPath("$.dailyLogTypes[4].code").isEqualTo(ATTENDANCE.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[4].label").isEqualTo(ATTENDANCE.getLabel());
        result.bodyJson().extractingPath("$.dailyLogTypes[5].code").isEqualTo(ETC.name());
        result.bodyJson().extractingPath("$.dailyLogTypes[5].label").isEqualTo(ETC.getLabel());

        then(dailyLogFinder).should().findDailyLogTypes();
    }


    private void assertDailyLogResponse(MvcTestResultAssert result, DailyLog dailyLog) {
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].dailyLogId")
                .isEqualTo(dailyLog.getDailyLogId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].logOn")
                .isEqualTo(dailyLog.getLogOn().toString());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].activityTime.startOn")
                .isEqualTo(dailyLog.getStartOn().format(ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].activityTime.endOn")
                .isEqualTo(dailyLog.getEndOn().format(ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].dailyLogType")
                .isEqualTo(dailyLog.getDailyLogType().name());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].activityTime.focusTimeSec")
                .isEqualTo(dailyLog.getFocusTimeSec().intValue());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].activityTime.focusRateExcluded")
                .isEqualTo(dailyLog.isFocusRateExcluded());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].contents")
                .isEqualTo(dailyLog.getContents());
    }

    private static Stream<DailyLogCreateRequest> invalidCreateRequests() {
        return DailyLogRequestFixture.invalidCreateRequests();
    }

    private static Stream<DailyLogUpdateRequest> invalidUpdateRequests() {
        return DailyLogRequestFixture.invalidUpdateRequests();
    }
}
