package net.akive.api.attendance.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.attendance.adapter.persistence.entity.AttendanceProofEntity;
import net.akive.api.attendance.application.required.AttendanceProofRepository;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.time.CurrentTimeProvider;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import static net.akive.api.common.time.ServiceDayPolicy.toServiceDate;

@Repository
@RequiredArgsConstructor
class AttendanceProofJpaAdapter implements AttendanceProofRepository {

    private final AttendanceProofJpaRepository attendanceProofJpaRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public AttendanceProof save(AttendanceProof attendanceProof) {
        return attendanceProofJpaRepository
                .save(AttendanceProofEntity.from(attendanceProof))
                .to(toServiceDate(currentTimeProvider.now()));
    }

    @Override
    public Optional<AttendanceProof> findByUserAndProofOn(Long userId, LocalDate proofOn) {
        return attendanceProofJpaRepository
                .findByUser_UserIdAndProofOn(userId, proofOn)
                .map(entity -> entity.to(toServiceDate(currentTimeProvider.now())));
    }

    @Override
    public boolean existsByUserIdAndProofOn(Long userId, LocalDate proofOn) {
        return attendanceProofJpaRepository.existsByUser_UserIdAndProofOn(userId, proofOn);
    }

    @Override
    public void delete(AttendanceProof attendanceProof) {
        attendanceProofJpaRepository.delete(AttendanceProofEntity.from(attendanceProof));
    }
}
