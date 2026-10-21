package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.SessionWeek;

public record SessionWeekResponse(int weekNum, WeekPeriodResponse weekPeriod) {

    public static SessionWeekResponse from(SessionWeek sessionWeek) {
        return new SessionWeekResponse(
                sessionWeek.weekNum(),
                WeekPeriodResponse.from(sessionWeek.weekPeriod())
        );
    }
}
