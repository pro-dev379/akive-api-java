package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.WeekPeriod;

import java.time.LocalDate;

public record WeekPeriodResponse(LocalDate startOn, LocalDate endOn) {

    public static WeekPeriodResponse from(WeekPeriod weekPeriod) {
        return new WeekPeriodResponse(weekPeriod.startOn(), weekPeriod.endOn());
    }
}
