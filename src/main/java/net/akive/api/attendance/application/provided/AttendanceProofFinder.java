package net.akive.api.attendance.application.provided;

import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.ResourceNotFoundException;

import java.time.LocalDate;

public interface AttendanceProofFinder {

    AttendanceProof findOrThrow(Long userId, LocalDate proofOn) throws ResourceNotFoundException;
}
