package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.*;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Goal {

    private Long goalId;
    private Long userId;
    private LocalTime attendOn;
    private long rest10MinSec;
    private int mealNum;
    private long meal1TimeSec;

    public static Goal create(
            Long goalId,
            Long userId,
            LocalTime attendOn,
            long rest10MinSec,
            int mealNum,
            long meal1TimeSec
    ) {
        return Goal.builder()
                .goalId(goalId)
                .userId(requirePositive(userId))
                .attendOn(requireNonNull(attendOn))
                .rest10MinSec(requireNonNegative(rest10MinSec))
                .mealNum(requireNonNegative(mealNum))
                .meal1TimeSec(requireNonNegative(meal1TimeSec))
                .build();
    }

    public static Goal createDefault(Long userId) {
        return Goal.builder()
                .userId(requirePositive(userId))
                .attendOn(DEFAULT_GOAL_ATTEND_ON)
                .rest10MinSec(DEFAULT_GOAL_REST_10_MIN_SEC)
                .mealNum(DEFAULT_GOAL_MEAL_NUM)
                .meal1TimeSec(DEFAULT_GOAL_MEAL_1_TIME_SEC)
                .build();
    }
}
