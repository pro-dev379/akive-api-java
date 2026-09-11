package net.akive.api.user.adapter.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.akive.api.user.domain.Goal;

import java.time.LocalTime;

public record GoalFindResponse(
        @JsonFormat(pattern = "HH:mm")
        LocalTime attendOn,
        long rest10MinSec,
        int mealNum,
        long meal1TimeSec
) {

    public static GoalFindResponse from(Goal goal) {
        return new GoalFindResponse(
                goal.getAttendOn(),
                goal.getRest10MinSec(),
                goal.getMealNum(),
                goal.getMeal1TimeSec()
        );
    }
}
