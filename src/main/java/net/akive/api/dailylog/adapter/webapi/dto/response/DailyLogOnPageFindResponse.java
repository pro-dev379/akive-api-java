package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.common.pagination.PageInfo;
import net.akive.api.common.pagination.PageResult;

import java.time.LocalDate;
import java.util.List;

public record DailyLogOnPageFindResponse(
        PageInfo pageInfo,
        List<LocalDate> logOns
) {

    public static DailyLogOnPageFindResponse from(PageResult<LocalDate> logOnPage) {
        return new DailyLogOnPageFindResponse(
                logOnPage.pageInfo(),
                logOnPage.contents()
        );
    }
}