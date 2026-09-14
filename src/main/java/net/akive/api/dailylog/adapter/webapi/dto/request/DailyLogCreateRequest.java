package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyLogCreateRequest(
        @NotNull LocalDate logOn,
        @NotNull @Valid FocusTimeRequest focusTime,
        @NotNull DailyLogType dailyLogType,
        @NotBlank String contents
) {

    public DailyLogCreateCommand to(Long userId, LocalDateTime now) {
        return new DailyLogCreateCommand(
                userId,
                this.logOn,
                this.focusTime.to(now),
                this.dailyLogType,
                this.contents,
                now
        );
    }

    public record FocusTimeRequest(
            @NotNull LocalDateTime startOn,
            @NotNull LocalDateTime endOn,
            @Min(0) Long focusTimeSec,
            boolean focusRateExcluded
    ) {

        public FocusTime to(LocalDateTime now) {
            return FocusTime.of(this.startOn, this.endOn, this.focusTimeSec, this.focusRateExcluded, now);
        }
    }
}
