package net.akive.api.user.application.provided.dto;

import java.time.LocalTime;

import static net.akive.api.common.domain.DomainValidator.*;

public record GoalCreateCommand(
        Long userId,
        LocalTime attendOn,
        long rest10MinSec,
        int mealNum,
        long meal1TimeSec
) {

    public GoalCreateCommand {
        requirePositive(userId);
        requireNonNull(attendOn);
        requireNonNegative(rest10MinSec);
        requireNonNegative(mealNum);
        requireNonNegative(meal1TimeSec);
    }
}
