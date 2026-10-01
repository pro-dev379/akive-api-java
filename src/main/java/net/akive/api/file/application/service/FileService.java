package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.core.config.file.CdnProperties;
import net.akive.api.file.application.provided.FileManager;
import net.akive.api.file.application.provided.ImageUrlFinder;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.application.required.ImageAssetIdGenerator;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.ImageType;
import net.akive.api.file.domain.UploadUrl;
import net.akive.api.file.domain.VariantType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.FilePolicy.validateMaxFileSize;

@Service
@Transactional
@RequiredArgsConstructor
class FileService implements FileManager, ImageUrlFinder {

    private final FileStorage fileStorage;
    private final ImageAssetRepository imageAssetRepository;
    private final ImageAssetIdGenerator imageAssetIdGenerator;
    private final CdnProperties cdnProperties;

    @Override
    public UploadUrlCreateResult createUploadUrl(UploadUrlCreateCommand command)
            throws BusinessRuleViolationException {
        validateMaxFileSize(command.imageType(), command.fileSize());

        String extension = extensionOf(command.imageType(), command.contentType());
        ImageAsset imageAsset = registerImageAsset(command.imageType());
        String fileKey = createOriginalFileKey(imageAsset, extension);

        UploadUrl uploadUrl = fileStorage.createUploadUrl(
                fileKey,
                command.contentType(),
                command.fileSize(),
                UPLOAD_URL_EXPIRATION_DURATION
        );

        return new UploadUrlCreateResult(
                imageAsset.getImageAssetId(),
                uploadUrl.value(),
                uploadUrl.expiresAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<URI> findImageUrl(UUID imageAssetId, VariantType variantType) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);

        return imageAsset.getVariants().stream()
                .filter(variant -> variant.getRecipeVersion().equals(imageAsset.getActiveRecipeVersion()))
                .filter(variant -> variant.getVariantType() == variantType)
                .findFirst()
                .map(variant -> cdnProperties.resolve(variant.getObjectKey()));
    }

    @Override
    public void deleteImageAsset(UUID imageAssetId) throws ResourceNotFoundException {
        ImageAsset imageAsset = findImageAssetOrThrow(imageAssetId);

        if (imageAsset.getMasterObjectKey() != null) {
            fileStorage.delete(imageAsset.getMasterObjectKey());
        }
        imageAsset.getVariants().forEach(variant -> fileStorage.delete(variant.getObjectKey()));

        imageAssetRepository.delete(imageAsset);
    }

    private ImageAsset registerImageAsset(ImageType imageType) {
        ImageAsset imageAsset = ImageAsset.create(imageAssetIdGenerator.generate(), imageType);

        return imageAssetRepository.save(imageAsset);
    }

    private String createOriginalFileKey(ImageAsset imageAsset, String extension) {
        String imageTypePath = switch (imageAsset.getImageType()) {
            case PROFILE_IMAGE -> "profile-images";
            case ATTENDANCE_PROOF -> "attendance-proofs";
        };

        return "uploads/%s/%s/original.%s".formatted(
                imageTypePath,
                imageAsset.getImageAssetId(),
                extension
        );
    }

    private String extensionOf(ImageType imageType, String contentType) {
        return switch (imageType) {
            case PROFILE_IMAGE, ATTENDANCE_PROOF -> switch (contentType) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> throw new BusinessRuleViolationException(FILE_CONTENT_TYPE_NOT_ALLOWED);
            };
        };
    }

    private ImageAsset findImageAssetOrThrow(UUID imageAssetId) {
        return imageAssetRepository.findByImageAssetId(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
    }
}
