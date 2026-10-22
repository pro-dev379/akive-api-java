package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.image.application.provided.UnusedImageAssetCleaner;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class UnusedImageAssetCleanupService implements UnusedImageAssetCleaner {

    private final ImageAssetRepository imageAssetRepository;
    private final ImageAssetDeleteService imageAssetDeleteService;

    @Override
    public int cleanupCreatedBefore(Instant createdBefore, int batchSize) {
        List<UUID> attendanceProofImageAssetIds = imageAssetRepository
                .findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(createdBefore, batchSize);
        List<UUID> profileImageAssetIds = imageAssetRepository
                .findUnreferencedProfileImageAssetIdsCreatedBefore(createdBefore, batchSize);

        return deleteAll(attendanceProofImageAssetIds, ImageType.ATTENDANCE_PROOF)
                + deleteAll(profileImageAssetIds, ImageType.PROFILE_IMAGE);
    }

    private int deleteAll(List<UUID> imageAssetIds, ImageType imageType) {
        int deletedCount = 0;

        for (UUID imageAssetId : imageAssetIds) {
            if (imageAssetDeleteService.deleteIfUnused(imageAssetId, imageType)) {
                deletedCount++;
            }
        }

        return deletedCount;
    }
}
