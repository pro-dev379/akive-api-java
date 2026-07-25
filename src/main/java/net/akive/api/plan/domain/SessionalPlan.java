package net.akive.api.plan.domain;

import lombok.*;
import net.akive.api.common.exception.business.BadRequestException;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_SESSION_PERIOD;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class SessionalPlan {

    private static final int SESSION_NAME_MAX_LENGTH = 50;

    private Long sessionalPlanId;
    private Long userId;
    private String sessionName;
    private SessionPeriod sessionPeriod;

    public static SessionalPlan createForUser(
            Long userId,
            String sessionName,
            SessionPeriod sessionPeriod,
            LocalDateTime requestedAt
    ) {
        validateSchedulableSessionPeriod(sessionPeriod, requestedAt);

        return SessionalPlan.builder()
                .userId(requirePositive(userId))
                .sessionName(requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH))
                .sessionPeriod(sessionPeriod)
                .build();
    }

    public static SessionalPlan create(
            Long sessionalPlanId,
            Long userId,
            String sessionName,
            SessionPeriod sessionPeriod
    ) {
        return SessionalPlan.builder()
                .sessionalPlanId(sessionalPlanId)
                .userId(requirePositive(userId))
                .sessionName(requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH))
                .sessionPeriod(requireNonNull(sessionPeriod))
                .build();
    }

    public SessionalPlan update(
            String sessionName,
            SessionPeriod sessionPeriod,
            LocalDateTime requestedAt
    ) {
        validateSchedulableSessionPeriod(sessionPeriod, requestedAt);

        return SessionalPlan.builder()
                .sessionalPlanId(this.sessionalPlanId)
                .userId(this.userId)
                .sessionName(requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH))
                .sessionPeriod(sessionPeriod)
                .build();
    }

    private static void validateSchedulableSessionPeriod(
            SessionPeriod sessionPeriod,
            LocalDateTime requestedAt
    ) {
        requireNonNull(sessionPeriod);
        requireNonNull(requestedAt);

        if (sessionPeriod.startOn().isBefore(requestedAt.toLocalDate()) ||
                !sessionPeriod.endOn().isAfter(requestedAt.toLocalDate())) {
            throw new BadRequestException(INVALID_SESSION_PERIOD);
        }
    }
}
