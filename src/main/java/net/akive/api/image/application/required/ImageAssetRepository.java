package net.akive.api.image.application.required;

import net.akive.api.image.domain.ImageAsset;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageAssetRepository {

    ImageAsset save(ImageAsset imageAsset);

    Optional<ImageAsset> findByImageAssetId(UUID imageAssetId);

    Optional<ImageAsset> findByImageAssetIdForUpdate(UUID imageAssetId);

    Optional<ImageAsset> findUnreferencedProfileImageAssetByIdForUpdate(UUID imageAssetId);

    Optional<ImageAsset> findUnreferencedAttendanceProofImageAssetByIdForUpdate(UUID imageAssetId);

    List<UUID> findUnreferencedProfileImageAssetIdsCreatedBefore(Instant createdBefore, int limit);

    List<UUID> findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(Instant createdBefore, int limit);

    void delete(ImageAsset imageAsset);
}
