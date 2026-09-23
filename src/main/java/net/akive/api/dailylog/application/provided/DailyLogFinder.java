package net.akive.api.dailylog.application.provided;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsFindQuery;
import net.akive.api.dailylog.application.provided.dto.FocusMetricsSeriesFindQuery;
import net.akive.api.dailylog.application.provided.dto.TimeUsageRatesFindQuery;
import net.akive.api.dailylog.domain.*;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogFinder {

    List<DailyLog> findAll(Long userId, LocalDate logOn);

    PageResult<LocalDate> findLogOnPage(Long userId, PageCondition pageCondition);

    List<DailyLogType> findDailyLogTypes();

    TimeUsageRates findTimeUsageRates(TimeUsageRatesFindQuery query);

    FocusMetrics findFocusMetrics(FocusMetricsFindQuery query);

    FocusMetricsSeries findFocusMetricsSeries(FocusMetricsSeriesFindQuery query);
}
