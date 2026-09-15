package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.application.provided.dto.DailyLogUpdateCommand;
import net.akive.api.dailylog.domain.DailyLogType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyLogUpdateRequest(
        @NotNull LocalDate targetLogOn,
        @NotNull @Valid FocusTimeRequest targetFocusTime,
        @NotNull DailyLogType targetDailyLogType,
        @NotBlank String targetContents
) {

    public DailyLogUpdateCommand to(Long dailyLogId, Long userId, LocalDateTime requestedAt) {
        return new DailyLogUpdateCommand(
                dailyLogId,
                userId,
                this.targetLogOn,
                this.targetFocusTime.to(requestedAt),
                this.targetDailyLogType,
                this.targetContents,
                requestedAt
        );
    }
}
