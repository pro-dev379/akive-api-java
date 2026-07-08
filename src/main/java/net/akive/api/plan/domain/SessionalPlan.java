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
            boolean duplicateSessionNameExists,
            LocalDateTime requestedAt
    ) {
        validateSessionNameDuplication(duplicateSessionNameExists);
        validateSessionPeriod(sessionStartOn, sessionEndOn, requestedAt);

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

    public SessionalPlan update(
            String sessionName,
            LocalDate sessionStartOn,
            LocalDate sessionEndOn,
            boolean duplicateSessionNameExists,
            boolean hasTotalQuantity,
            LocalDateTime requestedAt
    ) {
        validateSessionNameDuplication(duplicateSessionNameExists);
        validateSessionPeriod(sessionStartOn, sessionEndOn, requestedAt);
        validateSessionPeriodChangeAllowed(sessionStartOn, sessionEndOn, hasTotalQuantity);

        return SessionalPlan.builder()
                .sessionalPlanId(this.sessionalPlanId)
                .userId(this.userId)
                .sessionName(sessionName)
                .sessionStartOn(sessionStartOn)
                .sessionEndOn(sessionEndOn)
                .build();
    }

    private static void validateSessionPeriod(
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
    }

    private static void validateSessionNameDuplication(boolean duplicateSessionNameExists) {
        if (duplicateSessionNameExists) {
            throw new SessionalPlanException(SESSION_NAME_ALREADY_EXISTS);
        }
    }

    private void validateSessionPeriodChangeAllowed(
            LocalDate sessionStartOn,
            LocalDate sessionEndOn,
            boolean hasTotalQuantity
    ) {
        if (hasTotalQuantity &&
                (!this.sessionStartOn.equals(sessionStartOn) || !this.sessionEndOn.equals(sessionEndOn))) {
            throw new SessionalPlanException(SESSION_PERIOD_CHANGE_NOT_ALLOWED);
        }
    }
}