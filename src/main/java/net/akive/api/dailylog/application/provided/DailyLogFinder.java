package net.akive.api.dailylog.application.provided;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.DailyLog;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogFinder {

    List<DailyLog> findAll(Long userId, LocalDate logOn);

    PageResult<LocalDate> findLogOns(Long userId, PageCondition pageCondition);
}
