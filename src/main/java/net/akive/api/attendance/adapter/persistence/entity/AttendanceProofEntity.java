package net.akive.api.attendance.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.attendance.domain.AttendanceProof;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.user.adapter.persistence.entity.UserEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "attendance_proof",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-attendance_proof-user_id-proof_on",
                        columnNames = {"user_id", "proof_on"}
                ),
                @UniqueConstraint(
                        name = "ux-attendance_proof-image_asset_id",
                        columnNames = "image_asset_id"
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class AttendanceProofEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long attendanceProofId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate proofOn;

    @OneToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false)
    private ImageAssetEntity proofImageAsset;

    @Column(
            name = "is_late",
            nullable = false,
            columnDefinition = "TINYINT(1) UNSIGNED NOT NULL"
    )
    private Byte late;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean isLate() {
        if (this.late == null || (this.late != 0 && this.late != 1)) {
            throw new InvalidPersistedDataException("Invalid late value: " + this.late);
        }

        return this.late == 1;
    }

    private static byte lateValue(boolean late) {
        return late ? (byte) 1 : (byte) 0;
    }

    public static AttendanceProofEntity from(AttendanceProof attendanceProof) {
        AttendanceProofEntity attendanceProofEntity = new AttendanceProofEntity();
        attendanceProofEntity.attendanceProofId = attendanceProof.getAttendanceProofId();
        attendanceProofEntity.user = UserEntity.from(attendanceProof.getUser());
        attendanceProofEntity.proofOn = attendanceProof.getProofOn();
        attendanceProofEntity.proofImageAsset = ImageAssetEntity.from(attendanceProof.getProofImageAsset());
        attendanceProofEntity.late = lateValue(attendanceProof.getLate());

        return attendanceProofEntity;
    }

    public AttendanceProof to(LocalDate currentDate) {
        try {
            return AttendanceProof.reconstruct(
                    this.attendanceProofId,
                    this.user.to(),
                    this.proofOn,
                    this.proofImageAsset.to(),
                    this.isLate(),
                    currentDate
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct AttendanceProof: " + this.attendanceProofId,
                    e
            );
        }
    }
}
