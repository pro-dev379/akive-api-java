package net.akive.api.dailylog.application.provided;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.TimeUsageRates;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogFinder {

    List<DailyLog> findAll(Long userId, LocalDate logOn);

    List<DailyLogType> findDailyLogTypes();

    TimeUsageRates findTimeUsageRates(TimeUsageRatesFindQuery query);

    PageResult<LocalDate> findLogOnPage(Long userId, PageCondition pageCondition);
}
