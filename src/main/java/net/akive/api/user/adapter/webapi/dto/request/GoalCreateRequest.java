package net.akive.api.user.adapter.webapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.akive.api.user.application.provided.dto.GoalCreateCommand;

import java.time.LocalTime;

public record GoalCreateRequest(
        @NotNull LocalTime attendOn,
        @Min(0) long rest10MinSec,
        @Min(0) int mealNum,
        @Min(0) long meal1TimeSec
) {

    public GoalCreateCommand to(Long userId) {
        return new GoalCreateCommand(userId, this.attendOn, this.rest10MinSec, this.mealNum, this.meal1TimeSec);
    }
}
