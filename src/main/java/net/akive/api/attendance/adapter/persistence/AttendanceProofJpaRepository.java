package net.akive.api.attendance.adapter.persistence;

import net.akive.api.attendance.adapter.persistence.entity.AttendanceProofEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

interface AttendanceProofJpaRepository extends JpaRepository<AttendanceProofEntity, Long> {

    @EntityGraph(attributePaths = {"user", "proofImageAsset", "proofImageAsset.variants"})
    Optional<AttendanceProofEntity> findByUser_UserIdAndProofOn(Long userId, LocalDate proofOn);

    boolean existsByUser_UserIdAndProofOn(Long userId, LocalDate proofOn);
}
