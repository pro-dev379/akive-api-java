package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.DailyLog;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

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
            FocusTimeResponse focusTime,
            DailyLogType dailyLogType,
            String contents
    ) {
        public static DailyLogResponse from(DailyLog dailyLog) {
            return new DailyLogResponse(
                    dailyLog.getDailyLogId(),
                    dailyLog.getLogOn(),
                    FocusTimeResponse.from(dailyLog.getFocusTime()),
                    dailyLog.getDailyLogType(),
                    dailyLog.getContents()
            );
        }
    }

    public record FocusTimeResponse(
            LocalDateTime startOn,
            LocalDateTime endOn,
            Long focusTimeSec,
            boolean focusRateExcluded
    ) {
        public static FocusTimeResponse from(FocusTime focusTime) {
            return new FocusTimeResponse(
                    focusTime.getStartOn(),
                    focusTime.getEndOn(),
                    focusTime.getFocusTimeSec(),
                    focusTime.isFocusRateExcluded()
            );
        }
    }
}
