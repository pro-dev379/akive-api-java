package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.required.data.DailyPlanData;
import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.QuantityUnit;
import net.akive.api.plan.domain.value.Subject;

import java.time.LocalDate;

import static net.akive.api.plan.domain.fixture.TotalQuantityFixture.*;

public class DailyPlanDataFixture {

    public static final Long DAILY_PLAN_SESSIONAL_PLAN_ID = 1L;
    public static final Long DAILY_PLAN_TOTAL_QUANTITY_ID = 2L;
    public static final Long DAILY_PLAN_WEEKLY_QUANTITY_ID = 3L;
    public static final Long DAILY_PLAN_DAILY_QUANTITY_ID = 4L;

    private DailyPlanDataFixture() {
    }

    public static DailyPlanData createDailyPlanData(LocalDate date) {
        return new DailyPlanData(
                DAILY_PLAN_SESSIONAL_PLAN_ID,
                DAILY_PLAN_TOTAL_QUANTITY_ID,
                DAILY_PLAN_WEEKLY_QUANTITY_ID,
                new Subject(FIRST_SUBJECT_NAME, SUBJECT_CATEGORY),
                DailyQuantity.reconstruct(
                        DAILY_PLAN_DAILY_QUANTITY_ID,
                        date,
                        DailyQuantityAmount.of(100, 30, 70)
                ),
                new QuantityUnit(QUANTITY_UNIT)
        );
    }
}
