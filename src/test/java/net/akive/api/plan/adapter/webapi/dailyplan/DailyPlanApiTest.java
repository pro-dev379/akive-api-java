package net.akive.api.plan.adapter.webapi.dailyplan;

import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.application.provided.dailyplan.DailyPlanFinder;
import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.application.result.DailyPlan;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.application.fixture.PlanDataFixture.createDailyPlanData;
import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.FIRST_DATE;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebMvcTest(DailyPlanApi.class)
class DailyPlanApiTest extends PrivateApiTestSupport {

    @MockitoBean
    DailyPlanFinder dailyPlanFinder;

    @Test
    void 일일_계획을_조회한다() {
        DailyPlan dailyPlan = createDailyPlan(FIRST_DATE);
        DailyPlansFindQuery query = new DailyPlansFindQuery(USER_ID, FIRST_DATE);

        given(dailyPlanFinder.findAll(query)).willReturn(List.of(dailyPlan));

        MvcTestResultAssert result = get(PlanApiPath.DAILY_PLAN, USER_ID, FIRST_DATE).hasStatusOk();

        assertDailyPlanResponse(result, dailyPlan);

        then(dailyPlanFinder).should().findAll(query);
    }

    @Test
    void 일일_계획이_없으면_빈_목록을_응답한다() {
        DailyPlansFindQuery query = new DailyPlansFindQuery(USER_ID, FIRST_DATE);

        given(dailyPlanFinder.findAll(query)).willReturn(List.of());

        MvcTestResultAssert result = get(PlanApiPath.DAILY_PLAN, USER_ID, FIRST_DATE).hasStatusOk();

        result.bodyJson().extractingPath("$.dailyPlans").asArray().isEmpty();

        then(dailyPlanFinder).should().findAll(query);
    }

    private DailyPlan createDailyPlan(LocalDate date) {
        DailyPlanData data = createDailyPlanData(date);

        return new DailyPlan(
                data.sessionalPlanId(),
                data.totalQuantityId(),
                data.weeklyQuantityId(),
                data.subject(),
                data.dailyQuantity(),
                data.quantityUnit()
        );
    }

    private void assertDailyPlanResponse(MvcTestResultAssert result, DailyPlan dailyPlan) {
        DailyQuantityAmount amount = dailyPlan.dailyQuantity().getDailyQuantityAmount();

        result.bodyJson()
                .extractingPath("$.dailyPlans[0].sessionalPlanId")
                .isEqualTo(dailyPlan.sessionalPlanId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].totalQuantityId")
                .isEqualTo(dailyPlan.totalQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].weeklyQuantityId")
                .isEqualTo(dailyPlan.weeklyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].dailyQuantityId")
                .isEqualTo(dailyPlan.dailyQuantity().getDailyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].subject.name")
                .isEqualTo(dailyPlan.subject().name());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].subject.category")
                .isEqualTo(dailyPlan.subject().category());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].amount.quantityAmount")
                .isEqualTo(amount.quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].amount.completedQuantityAmount")
                .isEqualTo(amount.completedQuantityAmount().value());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].amount.delayedQuantityAmount")
                .isEqualTo(amount.delayedQuantityAmount().value());
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].amount.quantityUnit")
                .isEqualTo(dailyPlan.quantityUnit().value());
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.quantityMemo").isNull();
    }
}
