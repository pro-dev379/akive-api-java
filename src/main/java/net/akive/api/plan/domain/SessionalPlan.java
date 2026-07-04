package net.akive.api.plan.domain;

import lombok.*;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class SessionalPlan {

    private Long sessionalPlanId;
    private Long userId;
    private String sessionName;
    private LocalDate sessionStartOn;
    private LocalDate sessionEndOn;

    public static SessionalPlan create(
            Long sessionalPlanId,
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn
    ) {
        return SessionalPlan.builder()
                .sessionalPlanId(sessionalPlanId)
                .userId(userId)
                .sessionName(sessionName)
                .sessionStartOn(sessionStartOn)
                .sessionEndOn(sessionEndOn)
                .build();
    }
}