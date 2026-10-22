package net.akive.api.image.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.image.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ImageAssetJpaAdapter implements ImageAssetRepository {

    private final ImageAssetJpaRepository imageAssetJpaRepository;

    @Override
    public ImageAsset save(ImageAsset imageAsset) {
        return imageAssetJpaRepository.save(ImageAssetEntity.from(imageAsset)).to();
    }

    @Override
    public Optional<ImageAsset> findByImageAssetId(UUID imageAssetId) {
        return imageAssetJpaRepository.findById(imageAssetId).map(ImageAssetEntity::to);
    }

    @Override
    public Optional<ImageAsset> findByImageAssetIdForUpdate(UUID imageAssetId) {
        return imageAssetJpaRepository.findForUpdateByImageAssetId(imageAssetId).map(ImageAssetEntity::to);
    }

    @Override
    public Optional<ImageAsset> findUnreferencedProfileImageAssetByIdForUpdate(UUID imageAssetId) {
        return imageAssetJpaRepository
                .findUnreferencedProfileImageAssetByIdForUpdate(imageAssetId)
                .map(ImageAssetEntity::to);
    }

    @Override
    public Optional<ImageAsset> findUnreferencedAttendanceProofImageAssetByIdForUpdate(UUID imageAssetId) {
        return imageAssetJpaRepository
                .findUnreferencedAttendanceProofImageAssetByIdForUpdate(imageAssetId)
                .map(ImageAssetEntity::to);
    }

    @Override
    public List<UUID> findUnreferencedProfileImageAssetIdsCreatedBefore(Instant createdBefore, int limit) {
        return imageAssetJpaRepository.findUnreferencedProfileImageAssetIdsCreatedBefore(
                createdBefore,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public List<UUID> findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(Instant createdBefore, int limit) {
        return imageAssetJpaRepository.findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(
                createdBefore,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public void delete(ImageAsset imageAsset) {
        imageAssetJpaRepository.delete(ImageAssetEntity.from(imageAsset));
    }
}
