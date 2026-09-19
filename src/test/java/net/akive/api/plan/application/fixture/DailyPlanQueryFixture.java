package net.akive.api.plan.application.fixture;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;

import java.time.LocalDate;

import static net.akive.api.plan.domain.fixture.DailyQuantityFixture.FIRST_DATE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class DailyPlanQueryFixture {

    private DailyPlanQueryFixture() {
    }

    public static DailyPlansFindQuery createDailyPlansFindQuery() {
        return createDailyPlansFindQuery(FIRST_DATE);
    }

    public static DailyPlansFindQuery createDailyPlansFindQuery(LocalDate date) {
        return new DailyPlansFindQuery(USER_ID, date);
    }
}
