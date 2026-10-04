package net.akive.api.image.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.image.application.provided.UploadUrlCreator;
import net.akive.api.image.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.image.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.image.application.required.ImageAssetIdGenerator;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.UploadUrl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.IMAGE_CONTENT_TYPE_NOT_ALLOWED;
import static net.akive.api.image.domain.ImagePolicy.UPLOAD_URL_EXPIRATION_DURATION;
import static net.akive.api.image.domain.ImagePolicy.validateMaxFileSize;

@Service
@Transactional
@RequiredArgsConstructor
class UploadUrlCreateService implements UploadUrlCreator {

    private final ImageStorage imageStorage;
    private final ImageAssetRepository imageAssetRepository;
    private final ImageAssetIdGenerator imageAssetIdGenerator;

    @Override
    public UploadUrlCreateResult create(UploadUrlCreateCommand command)
            throws BusinessRuleViolationException {
        validateMaxFileSize(command.imageType(), command.fileSize());

        String extension = extensionOf(command.imageType(), command.contentType());
        ImageAsset imageAsset = registerImageAsset(command.imageType());
        UploadUrl uploadUrl = imageStorage.createOriginalImageUploadUrl(
                imageAsset.getImageAssetId(),
                imageAsset.getImageType(),
                extension,
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

    private ImageAsset registerImageAsset(ImageType imageType) {
        ImageAsset imageAsset = ImageAsset.create(imageAssetIdGenerator.generate(), imageType);

        return imageAssetRepository.save(imageAsset);
    }

    private String extensionOf(ImageType imageType, String contentType) {
        return switch (imageType) {
            case PROFILE_IMAGE, ATTENDANCE_PROOF -> switch (contentType) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> throw new BusinessRuleViolationException(IMAGE_CONTENT_TYPE_NOT_ALLOWED);
            };
        };
    }
}
