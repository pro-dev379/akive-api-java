package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.ActivityTime;
import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DailyLogsFindResponse(List<DailyLogResponse> dailyLogs) {

    public static DailyLogsFindResponse from(List<DailyLog> dailyLogs) {
        return new DailyLogsFindResponse(dailyLogs.stream().map(DailyLogResponse::from).toList());
    }

    public record DailyLogResponse(
            Long dailyLogId,
            LocalDate logOn,
            ActivityTimeResponse activityTime,
            DailyLogType dailyLogType,
            String contents
    ) {
        public static DailyLogResponse from(DailyLog dailyLog) {
            return new DailyLogResponse(
                    dailyLog.getDailyLogId(),
                    dailyLog.getLogOn(),
                    ActivityTimeResponse.from(dailyLog.getActivityTime()),
                    dailyLog.getDailyLogType(),
                    dailyLog.getContents()
            );
        }
    }

    public record ActivityTimeResponse(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded
    ) {
        public static ActivityTimeResponse from(ActivityTime activityTime) {
            return new ActivityTimeResponse(
                    activityTime.getStartOn(),
                    activityTime.getEndOn(),
                    activityTime.getFocusTimeSec(),
                    activityTime.isFocusRateExcluded()
            );
        }
    }
}
