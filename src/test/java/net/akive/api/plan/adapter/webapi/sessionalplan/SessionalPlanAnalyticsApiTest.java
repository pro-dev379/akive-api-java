package net.akive.api.plan.adapter.webapi.sessionalplan;

import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.domain.value.CompletionRate;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.math.BigDecimal;
import java.time.LocalDate;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_DATE_RANGE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@WebMvcTest(SessionalPlanAnalyticsApi.class)
class SessionalPlanAnalyticsApiTest extends PrivateApiTestSupport {

    private static final String COMPLETION_RATE_METRICS_URL = "/sessional-plans/analytics/completion-rates/metrics";
    private static final LocalDate START_ON = LocalDate.of(2026, 9, 14);
    private static final LocalDate END_ON = LocalDate.of(2026, 9, 20);

    @MockitoBean
    SessionalPlanFinder sessionalPlanFinder;

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
}
