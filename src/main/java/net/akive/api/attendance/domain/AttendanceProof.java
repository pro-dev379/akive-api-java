package net.akive.api.attendance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.user.domain.User;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_DATE_IN_FUTURE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.ATTENDANCE_PROOF_NOT_YET_ALLOWED;
import static net.akive.api.common.time.ServiceDayPolicy.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class AttendanceProof {

    private static final Duration ATTENDANCE_OPEN_BEFORE = Duration.ofMinutes(5);

    private Long attendanceProofId;

    private User user;

    private LocalDate proofOn;

    private ImageAsset proofImageAsset;

    private Boolean late;

    public static AttendanceProof reconstruct(
            Long attendanceProofId,
            User user,
            LocalDate proofOn,
            ImageAsset proofImageAsset,
            Boolean late,
            LocalDate currentDate
    ) {
        validateProofOnNotFuture(proofOn, currentDate);

        return AttendanceProof.builder()
                .attendanceProofId(requirePositive(attendanceProofId))
                .user(requireNonNull(user))
                .proofOn(requireNonNull(proofOn))
                .proofImageAsset(requireNonNull(proofImageAsset))
                .late(requireNonNull(late))
                .build();
    }

    public static AttendanceProof submit(
            User user,
            ImageAsset proofImageAsset,
            LocalTime goalAttendOn,
            Instant requestedAt
    ) {
        LocalDate proofOn = toServiceDate(requestedAt);
        Instant targetAttendanceAt = resolveTargetAttendanceAt(proofOn, requireNonNull(goalAttendOn));

        validateAttendanceAllowed(requestedAt, targetAttendanceAt);

        boolean late = requestedAt.isAfter(targetAttendanceAt);

        return AttendanceProof.builder()
                .user(requireNonNull(user))
                .proofOn(requireNonNull(proofOn))
                .proofImageAsset(requireNonNull(proofImageAsset))
                .late(requireNonNull(late))
                .build();
    }

    private static Instant resolveTargetAttendanceAt(LocalDate proofOn, LocalTime goalAttendOn) {
        LocalDate targetDate = goalAttendOn.isBefore(LocalTime.of(DAILY_BOUNDARY_HOUR, 0))
                ? proofOn.plusDays(1)
                : proofOn;

        return toInstantAtServiceZone(targetDate.atTime(goalAttendOn));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttendanceProof that = (AttendanceProof) o;

        return this.attendanceProofId != null && Objects.equals(this.attendanceProofId, that.attendanceProofId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static void validateProofOnNotFuture(LocalDate proofOn, LocalDate currentDate) {
        requireNonNull(proofOn);
        requireNonNull(currentDate);

        if (proofOn.isAfter(currentDate)) {
            throw new BusinessRuleViolationException(ATTENDANCE_PROOF_DATE_IN_FUTURE);
        }
    }

    private static void validateAttendanceAllowed(Instant requestedAt, Instant targetAttendanceAt) {
        requireNonNull(requestedAt);

        if (requestedAt.isBefore(targetAttendanceAt.minus(ATTENDANCE_OPEN_BEFORE))) {
            throw new BusinessRuleViolationException(ATTENDANCE_PROOF_NOT_YET_ALLOWED);
        }
    }
}
