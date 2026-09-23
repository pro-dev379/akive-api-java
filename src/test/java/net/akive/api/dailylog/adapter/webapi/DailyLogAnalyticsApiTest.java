package net.akive.api.dailylog.adapter.webapi;

import net.akive.api.dailylog.application.provided.DailyLogFinder;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsFindQuery;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsSeriesFindQuery;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusMetrics;
import net.akive.api.dailylog.domain.FocusMetricsSeries;
import net.akive.api.dailylog.domain.TimeUsageRates;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_DATE_RANGE;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.*;
import static net.akive.api.dailylog.domain.FocusMetricsUnit.DAY;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@WebMvcTest(DailyLogAnalyticsApi.class)
class DailyLogAnalyticsApiTest extends PrivateApiTestSupport {

    private static final String TIME_USAGE_RATES_URL = "/daily-logs/analytics/analytics/time-usage-rates";
    private static final String FOCUS_METRICS_URL = "/daily-logs/analytics/focus/focusMetrics";
    private static final String FOCUS_METRICS_SERIES_URL = "/daily-logs/analytics/focus/series";

    @MockitoBean
    DailyLogFinder dailyLogFinder;

    @Test
    void 조회_기간_전체의_일일_로그_타입별_시간_사용률을_조회한다() {
        LocalDate startOn = LOG_ON;
        LocalDate endOn = LOG_ON.plusDays(1);
        DailyLog etcLog = etc(START_ON, END_ON, false);
        TimeUsageRatesFindQuery query = new TimeUsageRatesFindQuery(USER_ID, startOn, endOn);
        given(dailyLogFinder.findTimeUsageRates(query))
                .willReturn(TimeUsageRates.calculate(startOn, endOn, List.of(etcLog)));

        MvcTestResultAssert result = get(
                TIME_USAGE_RATES_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                startOn,
                endOn
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.timeUsageRates[0].code").isEqualTo("STUDY");
        result.bodyJson().extractingPath("$.timeUsageRates[0].usageRate").isEqualTo(0.0);
        result.bodyJson().extractingPath("$.timeUsageRates[5].code").isEqualTo("ETC");
        result.bodyJson().extractingPath("$.timeUsageRates[5].usageRate")
                .isEqualTo(2.1);
        result.bodyJson().extractingPath("$.timeUsageRates[6].code")
                .isEqualTo("UNRECORDED");
        result.bodyJson().extractingPath("$.timeUsageRates[6].label")
                .isEqualTo("미기록");
        result.bodyJson().extractingPath("$.timeUsageRates[6].usageRate")
                .isEqualTo(97.9);

        then(dailyLogFinder).should().findTimeUsageRates(query);
    }

    @Test
    void 일일_로그_타입별_시간_사용률_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                TIME_USAGE_RATES_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                LOG_ON.plusDays(1),
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 조회_기간의_집중_시간과_집중도를_조회한다() {
        LocalDate endOn = LOG_ON.plusDays(1);
        FocusMetricsFindQuery query = new FocusMetricsFindQuery(USER_ID, LOG_ON, endOn);
        given(dailyLogFinder.findFocusMetrics(query))
                .willReturn(new FocusMetrics(7200, new BigDecimal("75.0")));

        MvcTestResultAssert result = get(
                FOCUS_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                LOG_ON,
                endOn
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.focusTimeSec").isEqualTo(7200);
        result.bodyJson().extractingPath("$.focusRate").isEqualTo(75.0);
        then(dailyLogFinder).should().findFocusMetrics(query);
    }

    @Test
    void 조회_기간의_집중_시간과_집중도_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                FOCUS_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                LOG_ON.plusDays(1),
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 조회_기간의_집중_시간과_집중도_조회_시_날짜_형식이_올바르지_않으면_400을_응답한다() {
        get(FOCUS_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                "invalid-date",
                LOG_ON
        ).hasStatus(BAD_REQUEST);

        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 날짜별로_조회_기간의_집중_시간과_집중도를_조회한다() {
        LocalDate endOn = LOG_ON.plusDays(1);
        FocusMetricsSeriesFindQuery query = new FocusMetricsSeriesFindQuery(USER_ID, LOG_ON, endOn, DAY);
        FocusMetricsSeries series = new FocusMetricsSeries(List.of(
                new FocusMetricsSeries.PeriodFocusMetrics(
                        LOG_ON,
                        LOG_ON,
                        new FocusMetrics(3600, new BigDecimal("50.0"))
                ),
                new FocusMetricsSeries.PeriodFocusMetrics(
                        endOn,
                        endOn,
                        new FocusMetrics(7200, new BigDecimal("75.0"))
                )
        ));
        given(dailyLogFinder.findFocusMetricsSeries(query)).willReturn(series);

        MvcTestResultAssert result = get(
                FOCUS_METRICS_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                LOG_ON,
                endOn,
                DAY
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.periodFocusMetrics[0].startOn").isEqualTo(LOG_ON.toString());
        result.bodyJson().extractingPath("$.periodFocusMetrics[0].endOn").isEqualTo(LOG_ON.toString());
        result.bodyJson().extractingPath("$.periodFocusMetrics[0].focusTimeSec").isEqualTo(3600);
        result.bodyJson().extractingPath("$.periodFocusMetrics[0].focusRate").isEqualTo(50.0);
        result.bodyJson().extractingPath("$.periodFocusMetrics[1].startOn").isEqualTo(endOn.toString());
        result.bodyJson().extractingPath("$.periodFocusMetrics[1].focusTimeSec").isEqualTo(7200);
        result.bodyJson().extractingPath("$.periodFocusMetrics[1].focusRate").isEqualTo(75.0);
        then(dailyLogFinder).should().findFocusMetricsSeries(query);
    }

    @Test
    void 날짜별로_집중_시간과_집중도_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                FOCUS_METRICS_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                LOG_ON.plusDays(1),
                LOG_ON,
                DAY
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 날짜별로_집중_시간과_집중도_조회_시_지원하지_않는_조회_단위이면_400을_응답한다() {
        get(
            FOCUS_METRICS_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
            USER_ID,
            LOG_ON,
            LOG_ON,
            "invalid-unit"
        ).hasStatus(BAD_REQUEST);

        then(dailyLogFinder).shouldHaveNoInteractions();
    }

    @Test
    void 일일_로그_타입별_시간_사용률_조회_시_날짜_형식이_올바르지_않으면_400을_응답한다() {
        get(TIME_USAGE_RATES_URL + "?startOn={startOn}&endOn={endOn}", USER_ID, "invalid-date", LOG_ON)
                .hasStatus(BAD_REQUEST);

        then(dailyLogFinder).shouldHaveNoInteractions();
    }
}
