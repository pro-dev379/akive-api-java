package net.akive.api.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Goal {

    private Long goalId;
    private Long userId;
    private LocalTime attendOn;
    private Integer rest10MinSec;
    private Integer mealNum;
    private Integer meal1TimeSec;

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
