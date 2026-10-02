package net.akive.api.dailylog.adapter.webapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.dailylog.application.provided.dto.DailyLogCreateCommand;
import net.akive.api.dailylog.domain.DailyLogType;

public record DailyLogCreateRequest(
        @NotNull @Valid ActivityTimeRequest activityTime,
        @NotNull DailyLogType dailyLogType,
        @NotBlank String contents
) {

    public DailyLogCreateCommand to(Long userId) {
        return new DailyLogCreateCommand(
                userId,
                this.activityTime.to(),
                this.dailyLogType,
                this.contents
        );
    }
}
