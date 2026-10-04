package net.akive.api.attendance.application.required;

import net.akive.api.attendance.domain.AttendanceProof;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceProofRepository {

    AttendanceProof save(AttendanceProof attendanceProof);

    Optional<AttendanceProof> findByUserAndProofOn(Long userId, LocalDate proofOn);

    boolean existsByUserIdAndProofOn(Long userId, LocalDate proofOn);
}
