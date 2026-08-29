package net.akive.api.plan.adapter.webapi.weeklyplan;

import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.plan.adapter.webapi.common.PlanApiPath.WEEKLY_PLAN;
import static net.akive.api.plan.domain.PlanFixture.*;
import static net.akive.api.user.domain.UserFixture.createPersistedUser;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class WeeklyPlanApiTest extends PrivateApiTestSupport {

    @Test
    void 위클리_플랜을_조회한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Integer weekNum = 1;
        WeeklyPlan weeklyPlan = createWeeklyPlan(userId, sessionalPlanId, weekNum);
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum);

        given(weeklyPlanFinder.find(query)).willReturn(weeklyPlan);

        MvcTestResultAssert result = get(userId, WEEKLY_PLAN, sessionalPlanId, weekNum)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.sessionWeek.weekNum").isEqualTo(1);
        result.bodyJson().extractingPath("$.sessionWeek.startOn").isEqualTo("2026-01-01");
        result.bodyJson().extractingPath("$.sessionWeek.endOn").isEqualTo("2026-01-04");
        result.bodyJson().extractingPath("$.planDates[0].date").isEqualTo("2026-01-01");
        result.bodyJson().extractingPath("$.planDates[0].dayOfWeek").isEqualTo("THURSDAY");
        result.bodyJson().extractingPath("$.weeklyQuantities[0].sessionalPlanId").isEqualTo(10);
        result.bodyJson().extractingPath("$.weeklyQuantities[0].totalQuantityId").isEqualTo(20);
        result.bodyJson().extractingPath("$.weeklyQuantities[0].weeklyQuantityId").isEqualTo(1);
        result.bodyJson().extractingPath("$.weeklyQuantities[0].subject.name").isEqualTo(SUBJECT_NAME);
        result.bodyJson().extractingPath("$.weeklyQuantities[0].subject.category").isEqualTo(SUBJECT_CATEGORY);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityAmount")
                .isEqualTo(TOTAL_AMOUNT);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityUnit")
                .isEqualTo(QUANTITY_UNIT);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].weeklyQuantityAmount.quantityMemo")
                .isNull();
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityId")
                .isEqualTo(1);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].planDate.date")
                .isEqualTo("2026-01-01");
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.quantityAmount")
                .isEqualTo(100);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.completedQuantityAmount")
                .isEqualTo(30);
        result.bodyJson()
                .extractingPath("$.weeklyQuantities[0].dailyQuantities[0].dailyQuantityAmount.delayedQuantityAmount")
                .isEqualTo(70);

        then(weeklyPlanFinder).should().find(query);
    }

    @Test
    void 위클리_플랜에_주간_계획량이_없으면_빈_목록을_응답한다() {
        Long userId = 1L;
        Long sessionalPlanId = 10L;
        Integer weekNum = 1;
        SessionWeek sessionWeek = createSessionWeek(weekNum);
        WeeklyPlan weeklyPlan = new WeeklyPlan(sessionWeek, List.of());
        WeeklyPlanFindQuery query = new WeeklyPlanFindQuery(sessionalPlanId, userId, weekNum);

        given(weeklyPlanFinder.find(query)).willReturn(weeklyPlan);

        MvcTestResultAssert result = get(userId, WEEKLY_PLAN, sessionalPlanId, weekNum)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.sessionWeek.weekNum").isEqualTo(1);
        result.bodyJson().extractingPath("$.planDates").asArray().hasSize(4);
        result.bodyJson().extractingPath("$.weeklyQuantities").asArray().isEmpty();

        then(weeklyPlanFinder).should().find(query);
    }

    private WeeklyPlan createWeeklyPlan(
            Long userId,
            Long sessionalPlanId,
            Integer weekNum
    ) {
        User user = createPersistedUser(userId);
        SessionalPlan sessionalPlan = createPersistedSessionalPlan(sessionalPlanId, user);
        TotalQuantity totalQuantity = createPersistedTotalQuantity(20L, sessionalPlan);
        WeeklyQuantity weeklyQuantity = totalQuantity.getWeeklyQuantities().get(weekNum - 1);
        SessionWeek sessionWeek = createSessionWeek(weekNum);
        WeeklyPlan.WeeklyPlanItem weeklyPlanItem = new WeeklyPlan.WeeklyPlanItem(
                sessionalPlanId,
                totalQuantity.getTotalQuantityId(),
                totalQuantity.getSubject(),
                totalQuantity.getTotalQuantityAmount().quantityUnit(),
                weeklyQuantity
        );

        return new WeeklyPlan(sessionWeek, List.of(weeklyPlanItem));
    }

    private SessionWeek createSessionWeek(Integer weekNum) {
        return createSessionPeriod().splitIntoWeeks().get(weekNum - 1);
    }
}
