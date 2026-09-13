package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogTimeRange;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.LocalDateTime;
import java.util.List;

public record DailyLogsFindResponse(List<DailyLogResponse> dailyLogs) {

    public static DailyLogsFindResponse from(List<DailyLog> dailyLogs) {
        return new DailyLogsFindResponse(dailyLogs.stream().map(DailyLogResponse::from).toList());
    }

    public record DailyLogResponse(
            Long dailyLogId,
            DailyLogTimeRangeResponse timeRange,
            DailyLogType dailyLogType,
            Long focusTimeSec,
            String contents
    ) {
        public static DailyLogResponse from(DailyLog dailyLog) {
            return new DailyLogResponse(
                    dailyLog.getDailyLogId(),
                    DailyLogTimeRangeResponse.from(dailyLog.getTimeRange()),
                    dailyLog.getDailyLogType(),
                    dailyLog.getFocusTimeSec(),
                    dailyLog.getContents()
            );
        }
    }

    public record DailyLogTimeRangeResponse(
            LocalDateTime startOn,
            LocalDateTime endOn
    ) {
        public static DailyLogTimeRangeResponse from(DailyLogTimeRange timeRange) {
            return new DailyLogTimeRangeResponse(
                    timeRange.getStartOn(),
                    timeRange.getEndOn()
            );
        }
    }
}
