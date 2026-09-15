package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.domain.FocusTime;

import java.time.LocalDateTime;

public record FocusTimeRequest(
        @NotNull LocalDateTime startOn,
        @NotNull LocalDateTime endOn,
        @Min(0) Long focusTimeSec,
        boolean focusRateExcluded
) {

    public FocusTime to(LocalDateTime requestedAt) {
        return FocusTime.of(this.startOn, this.endOn, this.focusTimeSec, this.focusRateExcluded, requestedAt);
    }
}
