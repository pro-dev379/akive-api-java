package net.akive.api.dailylog.application.required;

import net.akive.api.common.pagination.PageCondition;
import net.akive.api.common.pagination.PageResult;
import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository {

    DailyLog save(DailyLog dailyLog);

    PageResult<LocalDate> findLogOnPageByUserId(Long userId, PageCondition pageCondition);

    Optional<DailyLog> findByDailyLogIdAndUserId(Long dailyLogId, Long userId);

    List<DailyLog> findAllByUserIdAndLogOnOrderByStartOn(Long userId, LocalDate logOn);

    List<DailyLog> findAllByUserIdAndOverlappingTimeRangeOrderByStartOnAsc(
            Long userId,
            LocalDateTime startOn,
            LocalDateTime endOn
    );

    boolean existsOverlappingTimeByUserIdAndActivityTime(Long userId, ActivityTime activityTime);

    boolean existsOverlappingTimeByUserIdAndActivityTimeAndDailyLogIdNot(
            Long userId,
            ActivityTime activityTime,
            Long dailyLogId
    );

    void delete(DailyLog dailyLog);
}
