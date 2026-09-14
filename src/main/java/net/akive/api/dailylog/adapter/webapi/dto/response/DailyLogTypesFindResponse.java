package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.DailyLogType;

import java.util.List;

public record DailyLogTypesFindResponse(List<DailyLogTypeResponse> dailyLogTypes) {

    public static DailyLogTypesFindResponse from(List<DailyLogType> dailyLogTypes) {
        List<DailyLogTypeResponse> dailyLogTypeResponses =
                dailyLogTypes.stream().map(DailyLogTypeResponse::from).toList();

        return new DailyLogTypesFindResponse(dailyLogTypeResponses);
    }

    public record DailyLogTypeResponse(String code, String label) {

        public static DailyLogTypeResponse from(DailyLogType dailyLogType) {
            return new DailyLogTypeResponse(dailyLogType.name(), dailyLogType.getLabel());
        }
    }
}
