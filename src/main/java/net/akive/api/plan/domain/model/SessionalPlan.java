package net.akive.api.plan.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.user.domain.User;

import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_SESSION_PERIOD;
import static net.akive.api.plan.domain.policy.PlanPolicy.SESSION_NAME_MAX_LENGTH;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class SessionalPlan {

    private Long sessionalPlanId;

    private User user;

    private String sessionName;

    private SessionPeriod sessionPeriod;

    public static SessionalPlan reconstruct(
            Long sessionalPlanId,
            User user,
            String sessionName,
            SessionPeriod sessionPeriod
    ) {
        return SessionalPlan.builder()
                .sessionalPlanId(requirePositive(sessionalPlanId))
                .user(requireNonNull(user))
                .sessionName(requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH))
                .sessionPeriod(requireNonNull(sessionPeriod))
                .build();
    }

    public static SessionalPlan create(
            User user,
            String sessionName,
            SessionPeriod sessionPeriod,
            LocalDateTime requestedAt
    ) {
        validateSchedulableSessionPeriod(sessionPeriod, requestedAt);

        return SessionalPlan.builder()
                .user(requireNonNull(user))
                .sessionName(requireMaxLength(requireNotBlank(sessionName), SESSION_NAME_MAX_LENGTH))
                .sessionPeriod(sessionPeriod)
                .build();
    }

    public void update(
            String targetSessionName,
            SessionPeriod targetSessionPeriod,
            LocalDateTime requestedAt
    ) {
        validateSchedulableSessionPeriod(targetSessionPeriod, requestedAt);

        this.sessionName = requireMaxLength(requireNotBlank(targetSessionName), SESSION_NAME_MAX_LENGTH);
        this.sessionPeriod = targetSessionPeriod;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionalPlan that = (SessionalPlan) o;

        return this.sessionalPlanId != null && Objects.equals(this.sessionalPlanId, that.sessionalPlanId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
