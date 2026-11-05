package net.akive.api.plan.adapter.webapi.sessionalplan;

import net.akive.api.common.cache.ResponseCache;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateSeriesFindQuery;
import net.akive.api.plan.domain.value.CompletionRate;
import net.akive.api.plan.domain.value.CompletionRateSeries;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.analytics.TimeSeriesUnit.DAY;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_DATE_RANGE;
import static net.akive.api.plan.domain.fixture.CompletionRateFixture.END_ON;
import static net.akive.api.plan.domain.fixture.CompletionRateFixture.START_ON;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@WebMvcTest(SessionalPlanAnalyticsApi.class)
class SessionalPlanAnalyticsApiTest extends PrivateApiTestSupport {

    private static final String COMPLETION_RATE_METRICS_URL = "/sessional-plans/analytics/completion-rates/metrics";
    private static final String COMPLETION_RATE_SERIES_URL = "/sessional-plans/analytics/completion-rates/series";
    @MockitoBean
    SessionalPlanFinder sessionalPlanFinder;

    @MockitoBean
    ResponseCache responseCache;

    @Test
    void 조회_기간의_달성률을_조회한다() {
        CompletionRateFindQuery query = new CompletionRateFindQuery(USER_ID, START_ON, END_ON);
        given(sessionalPlanFinder.findCompletionRate(query))
                .willReturn(new CompletionRate(new BigDecimal("75.0")));

        MvcTestResultAssert result = get(
                COMPLETION_RATE_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                START_ON,
                END_ON
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.completionRate").isEqualTo(75.0);
        then(sessionalPlanFinder).should().findCompletionRate(query);
    }

    @Test
    void 달성률_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                COMPLETION_RATE_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
                USER_ID,
                END_ON.plusDays(1),
                END_ON
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(sessionalPlanFinder).shouldHaveNoInteractions();
    }

    @Test
    void 달성률_조회_시_날짜_형식이_올바르지_않으면_400을_응답한다() {
        get(
            COMPLETION_RATE_METRICS_URL + "?startOn={startOn}&endOn={endOn}",
            USER_ID,
            "invalid-date",
            END_ON
        ).hasStatus(BAD_REQUEST);

        then(sessionalPlanFinder).shouldHaveNoInteractions();
    }

    @Test
    void 날짜별로_조회_기간의_달성률을_조회한다() {
        LocalDate endOn = START_ON.plusDays(1);
        CompletionRateSeriesFindQuery query = new CompletionRateSeriesFindQuery(USER_ID, START_ON, endOn, DAY);
        CompletionRateSeries series = new CompletionRateSeries(List.of(
                new CompletionRateSeries.PeriodCompletionRate(
                        START_ON,
                        START_ON,
                        new CompletionRate(new BigDecimal("50.0"))
                ),
                new CompletionRateSeries.PeriodCompletionRate(
                        endOn,
                        endOn,
                        new CompletionRate(new BigDecimal("75.0"))
                )
        ));
        given(sessionalPlanFinder.findCompletionRateSeries(query)).willReturn(series);

        MvcTestResultAssert result = get(
                COMPLETION_RATE_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                START_ON,
                endOn,
                DAY
        ).hasStatusOk();

        result.bodyJson().extractingPath("$.periodCompletionRates[0].startOn")
                .isEqualTo(START_ON.toString());
        result.bodyJson().extractingPath("$.periodCompletionRates[0].endOn")
                .isEqualTo(START_ON.toString());
        result.bodyJson().extractingPath("$.periodCompletionRates[0].completionRate").isEqualTo(50.0);
        result.bodyJson().extractingPath("$.periodCompletionRates[1].startOn")
                .isEqualTo(endOn.toString());
        result.bodyJson().extractingPath("$.periodCompletionRates[1].completionRate").isEqualTo(75.0);
        then(sessionalPlanFinder).should().findCompletionRateSeries(query);
    }

    @Test
    void 날짜별_달성률_조회_시_시작일이_종료일보다_늦으면_400을_응답한다() {
        MvcTestResultAssert result = get(
                COMPLETION_RATE_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                END_ON.plusDays(1),
                END_ON,
                DAY
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_DATE_RANGE);
        then(sessionalPlanFinder).shouldHaveNoInteractions();
    }

    @Test
    void 날짜별_달성률_조회_시_지원하지_않는_조회_단위이면_400을_응답한다() {
        get(
                COMPLETION_RATE_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                START_ON,
                END_ON,
                "invalid-unit"
        ).hasStatus(BAD_REQUEST);

        then(sessionalPlanFinder).shouldHaveNoInteractions();
    }

    @Test
    void 날짜별_달성률_조회_시_날짜_형식이_올바르지_않으면_400을_응답한다() {
        get(
                COMPLETION_RATE_SERIES_URL + "?startOn={startOn}&endOn={endOn}&unit={unit}",
                USER_ID,
                "invalid-date",
                END_ON,
                DAY
        ).hasStatus(BAD_REQUEST);

        then(sessionalPlanFinder).shouldHaveNoInteractions();
    }
}
