package net.akive.api.file.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.file.application.provided.FileManager;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.application.required.FileStorage;
import net.akive.api.file.application.required.ImageAssetIdGenerator;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.file.domain.ImageType;
import net.akive.api.file.domain.UploadUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.FILE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.file.domain.FilePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.file.domain.FilePolicy.validateMaxFileSize;

@Service
@Transactional
@RequiredArgsConstructor
class FileService implements FileManager {

    private final FileStorage fileStorage;
    private final ImageAssetRepository imageAssetRepository;
    private final ImageAssetIdGenerator imageAssetIdGenerator;

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

        return new UploadUrlCreateResult(fileKey, uploadUrl.value(), uploadUrl.expiresAt());
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
}
