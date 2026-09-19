package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;

import static net.akive.api.plan.domain.fixture.SessionalPlanFixture.SESSIONAL_PLAN_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class WeeklyPlanQueryFixture {

    private WeeklyPlanQueryFixture() {
    }

    public static WeeklyPlanFindQuery createWeeklyPlanFindQuery(int weekNum) {
        return new WeeklyPlanFindQuery(SESSIONAL_PLAN_ID, USER_ID, weekNum);
    }
}
