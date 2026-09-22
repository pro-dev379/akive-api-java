package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.domain.ActivityTime;

import java.time.LocalDateTime;

public record ActivityTimeRequest(
        @NotNull LocalDateTime startOn,
        @NotNull LocalDateTime endOn,
        @Min(0) Long focusTimeSec,
        boolean focusRateExcluded
) {

    public ActivityTime to(LocalDateTime requestedAt) {
        return ActivityTime.of(this.startOn, this.endOn, this.focusTimeSec, this.focusRateExcluded, requestedAt);
    }
}
