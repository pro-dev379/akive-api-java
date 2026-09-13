package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.domain.DailyLogTimeRange;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyLogCreateRequest(
        @NotNull @Valid DailyLogTimeRangeRequest timeRange,
        @NotNull DailyLogType dailyLogType,
        @Min(0) Long focusTimeSec,
        boolean focusRateExcluded,
        @NotBlank String contents
) {

    public DailyLogCreateCommand to(Long userId, LocalDate logOn, LocalDateTime now) {
        return new DailyLogCreateCommand(
                userId,
                logOn,
                DailyLogTimeRange.of(this.timeRange.startOn(), this.timeRange.endOn(), now),
                this.dailyLogType,
                this.focusTimeSec,
                this.focusRateExcluded,
                this.contents
        );
    }
}
