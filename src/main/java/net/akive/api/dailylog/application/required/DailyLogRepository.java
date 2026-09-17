package net.akive.api.dailylog.application.required;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository {

    DailyLog save(DailyLog dailyLog);

    PageResult<LocalDate> findLogOnPageByUserId(Long userId, PageCondition pageCondition);

    Optional<DailyLog> findByDailyLogIdAndUserId(Long dailyLogId, Long userId);

    List<DailyLog> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);

    boolean existsOverlappingTimeByUserIdAndFocusTime(Long userId, FocusTime focusTime);

    boolean existsOverlappingTimeByUserIdAndFocusTimeAndDailyLogIdNot(
            Long userId,
            FocusTime focusTime,
            Long dailyLogId
    );

    void delete(DailyLog dailyLog);
}
