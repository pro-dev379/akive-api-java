package net.akive.api.image.adapter.persistence;

import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

interface ImageAssetJpaRepository extends JpaRepository<ImageAssetEntity, UUID> {

    @Override
    @NotNull
    @EntityGraph(attributePaths = "variants")
    Optional<ImageAssetEntity> findById(@NotNull UUID imageAssetId);

    @Lock(PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "variants")
    Optional<ImageAssetEntity> findForUpdateByImageAssetId(UUID imageAssetId);

    @Lock(PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "variants")
    @Query("""
            select imageAsset
            from ImageAssetEntity imageAsset
            where imageAsset.imageAssetId = :imageAssetId
              and imageAsset.imageType = net.akive.api.image.domain.ImageType.PROFILE_IMAGE
              and not exists (
                  select user.userId
                  from UserEntity user
                  where user.userDetail.profileImageAssetId = imageAsset.imageAssetId
              )
    """)
    Optional<ImageAssetEntity> findUnreferencedProfileImageAssetByIdForUpdate(UUID imageAssetId);

    @Lock(PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "variants")
    @Query("""
            select imageAsset
            from ImageAssetEntity imageAsset
            where imageAsset.imageAssetId = :imageAssetId
              and imageAsset.imageType = net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF
              and not exists (
                  select attendanceProof.attendanceProofId
                  from AttendanceProofEntity attendanceProof
                  where attendanceProof.proofImageAsset.imageAssetId = imageAsset.imageAssetId
              )
    """)
    Optional<ImageAssetEntity> findUnreferencedAttendanceProofImageAssetByIdForUpdate(UUID imageAssetId);

    @Query("""
            select imageAsset.imageAssetId
            from ImageAssetEntity imageAsset
            where imageAsset.imageType = net.akive.api.image.domain.ImageType.PROFILE_IMAGE
              and imageAsset.createdAt < :createdBefore
              and not exists (
                  select user.userId
                  from UserEntity user
                  where user.userDetail.profileImageAssetId = imageAsset.imageAssetId
              )
            order by imageAsset.createdAt, imageAsset.imageAssetId
    """)
    List<UUID> findUnreferencedProfileImageAssetIdsCreatedBefore(Instant createdBefore, Pageable pageable);

    @Query("""
            select imageAsset.imageAssetId
            from ImageAssetEntity imageAsset
            where imageAsset.imageType = net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF
              and imageAsset.createdAt < :createdBefore
              and not exists (
                  select attendanceProof.attendanceProofId
                  from AttendanceProofEntity attendanceProof
                  where attendanceProof.proofImageAsset.imageAssetId = imageAsset.imageAssetId
              )
            order by imageAsset.createdAt, imageAsset.imageAssetId
    """)
    List<UUID> findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(
            Instant createdBefore,
            Pageable pageable
    );
}
