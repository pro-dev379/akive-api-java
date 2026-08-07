package net.akive.api.plan.application.provided.dto.result.common;

import net.akive.api.plan.domain.SessionWeek;

public record SessionWeekResult(
        int weekNum,
        WeekPeriodResult weekPeriod
) {

    public static SessionWeekResult from(SessionWeek sessionWeek) {
        return new SessionWeekResult(
                sessionWeek.weekNum(),
                new WeekPeriodResult(sessionWeek.weekPeriod().startOn(), sessionWeek.weekPeriod().endOn())
        );
    }
}
