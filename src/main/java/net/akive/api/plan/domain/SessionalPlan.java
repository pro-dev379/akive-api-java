package net.akive.api.plan.domain;

import lombok.*;
import net.akive.api.plan.domain.exception.SessionalPlanException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.*;

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
    private List<TotalQuantity> totalQuantities;

    public static SessionalPlan createForUser(
            Long userId,
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn,
            LocalDateTime requestedAt
    ) {
        if (!sessionStartOn.isBefore(sessionEndOn) ||
                sessionStartOn.isBefore(requestedAt.toLocalDate()) ||
                !sessionEndOn.isAfter(requestedAt.toLocalDate())
        ) {
            throw new SessionalPlanException(INVALID_SESSION_PERIOD);
        }

        return SessionalPlan.builder()
                .userId(userId)
                .sessionName(sessionName)
                .sessionStartOn(sessionStartOn)
                .sessionEndOn(sessionEndOn)
                .build();
    }

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