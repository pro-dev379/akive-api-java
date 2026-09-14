package net.akive.api.dailylog.application.required;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository {

    DailyLog save(DailyLog dailyLog);

    List<DailyLog> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);

    PageResult<LocalDate> findLogOnsByUserId(Long userId, PageCondition pageCondition);

    boolean existsOverlappingTimeByUserIdAndFocusTime(Long userId, FocusTime focusTime);

    boolean existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
            Long userId,
            FocusTime focusTime,
            Long dailyLogId
    );
}
