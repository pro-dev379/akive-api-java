package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;

import java.time.LocalDate;

import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.FIRST_DATE;
import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class PlanQueryFixture {

    private PlanQueryFixture() {
    }

    public static DailyPlansFindQuery createDailyPlansFindQuery() {
        return createDailyPlansFindQuery(FIRST_DATE);
    }

    public static DailyPlansFindQuery createDailyPlansFindQuery(LocalDate date) {
        return new DailyPlansFindQuery(USER_ID, date);
    }

    public static WeeklyPlanFindQuery createWeeklyPlanFindQuery(int weekNum) {
        return new WeeklyPlanFindQuery(SESSIONAL_PLAN_ID, USER_ID, weekNum);
    }
}
