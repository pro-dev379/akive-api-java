package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Goal {

    private Long goalId;
    private Long userId;
    private LocalTime attendOn;
    private Integer rest10MinSec;
    private Integer mealNum;
    private Integer meal1TimeSec;

    public static Goal create(
            Long goalId,
            Long userId,
            LocalTime attendOn,
            Integer rest10MinSec,
            Integer mealNum,
            Integer meal1TimeSec
    ) {
        return Goal.builder()
                .goalId(goalId)
                .userId(userId)
                .attendOn(attendOn)
                .rest10MinSec(rest10MinSec)
                .mealNum(mealNum)
                .meal1TimeSec(meal1TimeSec)
                .build();
    }

    public static Goal createDefault(Long userId) {
        return Goal.builder()
                .userId(userId)
                .attendOn(LocalTime.of(20, 0))
                .rest10MinSec(120)
                .mealNum(3)
                .meal1TimeSec(3_600)
                .build();
    }
}
