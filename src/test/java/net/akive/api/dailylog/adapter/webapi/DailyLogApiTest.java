package net.akive.api.dailylog.adapter.webapi;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.pagination.SortDirection.DESC;
import static net.akive.api.common.pagination.fixture.PaginationFixture.*;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.LOG_ON;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.createReconstructedDailyLog;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@WebMvcTest(DailyLogApi.class)
class DailyLogApiTest extends PrivateApiTestSupport {

    private static final String DAILY_LOGS_URL = "/daily-logs/{logOn}";
    private static final String DAILY_LOG_ONS_URL = "/daily-logs/log-ons";

    @MockitoBean
    DailyLogFinder dailyLogFinder;

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
                .extractingPath("$.dailyLogs[0].startOn")
                .isEqualTo(dailyLog.getStartOn().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].endOn")
                .isEqualTo(dailyLog.getEndOn().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].dailyLogType")
                .isEqualTo(dailyLog.getDailyLogType().name());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].intensiveTimeSec")
                .isEqualTo(dailyLog.getIntensiveTimeSec().intValue());
        result.bodyJson()
                .extractingPath("$.dailyLogs[0].contents")
                .isEqualTo(dailyLog.getContents());
    }
}
