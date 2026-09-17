package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.common.pagination.PageInfo;
import net.akive.api.common.pagination.PageResult;

import java.time.LocalDate;
import java.util.List;

public record DailyLogOnsFindResponse(
        PageInfo pageInfo,
        List<LocalDate> logOns
) {

    public static DailyLogOnsFindResponse from(PageResult<LocalDate> logOns) {
        return new DailyLogOnsFindResponse(
                logOns.pageInfo(),
                logOns.contents()
        );
    }
}