package net.akive.api.plan.adapter.webapi.dailyplan;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.result.DailyPlan;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.plan.domain.PlanFixture.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class DailyPlanApiTest extends PrivateApiTestSupport {

    @Test
    void 일일_계획을_조회한다() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);
        DailyPlan dailyPlan = createDailyPlan(date);
        DailyPlansFindQuery query = new DailyPlansFindQuery(userId, date);

        given(dailyPlanFinder.find(query)).willReturn(List.of(dailyPlan));

        MvcTestResultAssert result = get(userId, "/sessional-plans/daily-plans/{date}", date)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.dailyPlans[0].sessionalPlanId").isEqualTo(1);
        result.bodyJson().extractingPath("$.dailyPlans[0].totalQuantityId").isEqualTo(2);
        result.bodyJson().extractingPath("$.dailyPlans[0].weeklyQuantityId").isEqualTo(3);
        result.bodyJson()
                .extractingPath("$.dailyPlans[0].dailyQuantityId")
                .isEqualTo(4);
        result.bodyJson().extractingPath("$.dailyPlans[0].subject.name").isEqualTo(SUBJECT_NAME);
        result.bodyJson().extractingPath("$.dailyPlans[0].subject.category").isEqualTo(SUBJECT_CATEGORY);
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.quantityAmount").isEqualTo(100);
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.completedQuantityAmount").isEqualTo(30);
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.delayedQuantityAmount").isEqualTo(70);
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.quantityUnit").isEqualTo(QUANTITY_UNIT);
        result.bodyJson().extractingPath("$.dailyPlans[0].amount.quantityMemo").isNull();

        then(dailyPlanFinder).should().find(query);
    }

    @Test
    void 일일_계획이_없으면_빈_목록을_응답한다() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);
        DailyPlansFindQuery query = new DailyPlansFindQuery(userId, date);

        given(dailyPlanFinder.find(query)).willReturn(List.of());

        MvcTestResultAssert result = get(userId, "/sessional-plans/daily-plans/{date}", date)
                .hasStatusOk();

        result.bodyJson().extractingPath("$.dailyPlans").asArray().isEmpty();

        then(dailyPlanFinder).should().find(query);
    }

    private DailyPlan createDailyPlan(LocalDate date) {
        return new DailyPlan(
                1L,
                2L,
                3L,
                new Subject(SUBJECT_NAME, SUBJECT_CATEGORY),
                DailyQuantity.reconstruct(
                        4L,
                        date,
                        DailyQuantityAmount.of(100, 30, 70, null)
                ),
                new QuantityUnit(QUANTITY_UNIT)
        );
    }
}
