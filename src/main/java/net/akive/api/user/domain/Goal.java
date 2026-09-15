package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.user.domain.UserPolicy.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class Goal {

    private Long goalId;

    private User user;

    private LocalTime attendOn;

    private long rest10MinSec;

    private int mealNum;

    private long meal1TimeSec;

    public static Goal reconstruct(
            Long goalId,
            User user,
            LocalTime attendOn,
            long rest10MinSec,
            int mealNum,
            long meal1TimeSec
    ) {
        return Goal.builder()
                .goalId(requirePositive(goalId))
                .user(requireNonNull(user))
                .attendOn(requireNonNull(attendOn))
                .rest10MinSec(requireNonNegative(rest10MinSec))
                .mealNum(requireNonNegative(mealNum))
                .meal1TimeSec(requireNonNegative(meal1TimeSec))
                .build();
    }

    public static Goal create(
            User user,
            LocalTime attendOn,
            long rest10MinSec,
            int mealNum,
            long meal1TimeSec
    ) {
        return Goal.builder()
                .user(requireNonNull(user))
                .attendOn(requireNonNull(attendOn))
                .rest10MinSec(requireNonNegative(rest10MinSec))
                .mealNum(requireNonNegative(mealNum))
                .meal1TimeSec(requireNonNegative(meal1TimeSec))
                .build();
    }

    public static Goal createDefault(User user) {
        return create(
                user,
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Goal that = (Goal) o;

        return this.goalId != null && Objects.equals(this.goalId, that.goalId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
