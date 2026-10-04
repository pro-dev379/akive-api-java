package net.akive.api.attendance.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.attendance.application.provided.AttendanceProofFinder;
import net.akive.api.attendance.application.required.AttendanceProofRepository;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static net.akive.api.common.exception.business.ResourceNotFoundError.ATTENDANCE_PROOF_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class AttendanceProofQueryService implements AttendanceProofFinder {

    private final AttendanceProofRepository attendanceProofRepository;

    @Override
    public AttendanceProof findOrThrow(Long userId, LocalDate proofOn) throws ResourceNotFoundException {
        return attendanceProofRepository.findByUserAndProofOn(userId, proofOn)
                .orElseThrow(() -> new ResourceNotFoundException(ATTENDANCE_PROOF_NOT_FOUND));
    }
}
