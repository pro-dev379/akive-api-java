package net.akive.api.plan.adapter.webapi.weeklyplan;

import net.akive.api.plan.adapter.webapi.common.PlanApiPath;
import net.akive.api.plan.application.provided.weeklyplan.WeeklyPlanFinder;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.PlanDate;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.*;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.FIRST_TOTAL_QUANTITY_ID;
import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.createReconstructedTotalQuantity;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static net.akive.api.user.domain.UserFixture.createReconstructedUser;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebMvcTest(WeeklyPlanApi.class)
class WeeklyPlanApiTest extends PrivateApiTestSupport {

    @MockitoBean
    WeeklyPlanFinder weeklyPlanFinder;

    @Test
    void 위클리_플랜을_조회한다() {
        WeeklyPlan weeklyPlan = createWeeklyPlan(FIRST_WEEK_NUM);
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(SESSIONAL_PLAN_ID, USER_ID, FIRST_WEEK_NUM);

        given(weeklyPlanFinder.find(query)).willReturn(weeklyPlan);

        MvcTestResultAssert result = get(PlanApiPath.WEEKLY_PLAN, USER_ID, SESSIONAL_PLAN_ID, FIRST_WEEK_NUM)
                .hasStatusOk();

        assertWeeklyPlanResponse(result, weeklyPlan);

        then(weeklyPlanFinder).should().find(query);
    }

    @Test
    void 위클리_플랜에_주간_계획량이_없으면_빈_목록을_응답한다() {
        SessionWeek sessionWeek = createSessionWeek(FIRST_WEEK_NUM);
        WeeklyPlan weeklyPlan = new WeeklyPlan(sessionWeek, List.of());
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(SESSIONAL_PLAN_ID, USER_ID, FIRST_WEEK_NUM);

        given(weeklyPlanFinder.find(query)).willReturn(weeklyPlan);

        MvcTestResultAssert result = get(PlanApiPath.WEEKLY_PLAN, USER_ID, SESSIONAL_PLAN_ID, FIRST_WEEK_NUM)
                .hasStatusOk();

        result.bodyJson()
                .extractingPath("$.sessionWeek.weekNum")
                .isEqualTo(weeklyPlan.sessionWeek().weekNum());
        result.bodyJson().extractingPath("$.planDates").asArray().hasSize(4);
        result.bodyJson().extractingPath("$.weeklyQuantities").asArray().isEmpty();

        then(weeklyPlanFinder).should().find(query);
    }

    private WeeklyPlan createWeeklyPlan(Integer weekNum) {
        User user = createReconstructedUser(USER_ID);
        SessionalPlan sessionalPlan = createReconstructedSessionalPlan(SESSIONAL_PLAN_ID, user);
        TotalQuantity totalQuantity = createReconstructedTotalQuantity(FIRST_TOTAL_QUANTITY_ID, sessionalPlan);
        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().get(weekNum - 1);
        SessionWeek sessionWeek = createSessionWeek(weekNum);
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem = new WeeklyPlan.WeeklyPlanItem(
                SESSIONAL_PLAN_ID,
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                weeklyQuantity
        );

        return new WeeklyPlan(sessionWeek, List.of(weeklyPlanItem));
    }

    private SessionWeek createSessionWeek(Integer weekNum) {
        return SESSION_PERIOD.splitIntoWeeks().get(weekNum - 1);
    }

    private void assertWeeklyPlanResponse(MvcTestResultAssert result, WeeklyPlan weeklyPlan) {
        SessionWeek sessionWeek = weeklyPlan.sessionWeek();
        PlanDate firstPlanDate = weeklyPlan.planDates().getFirst();
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem = weeklyPlan.weeklyQuantities().getFirst();
        WeeklyQuantity weeklyQuantity = weeklyPlanItem.weeklyQuantity();
        DailyQuantity dailyQuantity = weeklyQuantity.getDailyQuantities().getFirst();
        DailyQuantityAmount dailyQuantityAmount = dailyQuantity.getDailyQuantityAmount();

        result.bodyJson().extractingPath("$.sessionWeek.weekNum").isEqualTo(sessionWeek.weekNum());
        result.bodyJson()
                .extractingPath("$.sessionWeek.startOn")
                .isEqualTo(sessionWeek.weekPeriod().startOn().toString());
        result.bodyJson()
                .extractingPath("$.sessionWeek.endOn")
                .isEqualTo(sessionWeek.weekPeriod().endOn().toString());
        result.bodyJson().extractingPath("$.planDates[0].date").isEqualTo(firstPlanDate.date().toString());
        result.bodyJson().extractingPath("$.planDates[0].dayOfWeek").isEqualTo(firstPlanDate.dayOfWeek().name());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].sessionalPlanId")
                .isEqualTo(weeklyPlanItem.sessionalPlanId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].totalQuantityId")
                .isEqualTo(weeklyPlanItem.totalQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityId")
                .isEqualTo(weeklyQuantity.getWeeklyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].subject.name")
                .isEqualTo(weeklyPlanItem.subject().name());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].subject.category")
                .isEqualTo(weeklyPlanItem.subject().category());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount")
                .isEqualTo(weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityUnit")
                .isEqualTo(weeklyPlanItem.quantityUnit().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityMemo")
                .isNull();
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityId")
                .isEqualTo(dailyQuantity.getDailyQuantityId().intValue());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].planDate.date")
                .isEqualTo(dailyQuantity.getDate().toString());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.quantityAmount")
                .isEqualTo(dailyQuantityAmount.quantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.completedQuantityAmount")
                .isEqualTo(dailyQuantityAmount.completedQuantityAmount().value());
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(dailyQuantityAmount.delayedQuantityAmount().value());
    }
}
