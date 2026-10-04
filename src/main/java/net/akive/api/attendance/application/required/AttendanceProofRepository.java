package net.akive.api.attendance.application.required;

import net.akive.api.attendance.domain.AttendanceProof;

import java.time.LocalDate;

public interface AttendanceProofRepository {

    AttendanceProof save(AttendanceProof attendanceProof);

    boolean existsByUserIdAndProofOn(Long userId, LocalDate proofOn);
}
