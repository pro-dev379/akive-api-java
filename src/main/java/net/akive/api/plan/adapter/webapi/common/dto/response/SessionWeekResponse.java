package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.SessionWeek;

import java.time.LocalDate;

public record SessionWeekResponse(int weekNum, LocalDate startOn, LocalDate endOn) {

    public static SessionWeekResponse from(SessionWeek sessionWeek) {
        return new SessionWeekResponse(
                sessionWeek.weekNum(),
                sessionWeek.weekPeriod().startOn(),
                sessionWeek.weekPeriod().endOn()
        );
    }
}
