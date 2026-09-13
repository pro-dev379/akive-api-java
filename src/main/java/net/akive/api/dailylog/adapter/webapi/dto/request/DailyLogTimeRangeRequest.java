package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DailyLogTimeRangeRequest(@NotNull LocalDateTime startOn, @NotNull LocalDateTime endOn) {
}
