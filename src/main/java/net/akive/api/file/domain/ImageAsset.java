package net.akive.api.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.LocalDateTime;
import java.util.*;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_IMAGE_ASSET_STATE;
import static net.akive.api.file.domain.FilePolicy.OBJECT_KEY_MAX_LENGTH;
import static net.akive.api.file.domain.ProcessingStatus.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class ImageAsset {

    private UUID imageAssetId;

    private ImageType imageType;

    private String masterObjectKey;

    private Integer activeRecipeVersion;

    private ProcessingStatus processingStatus;

    private String processingError;

    private LocalDateTime processedAt;

    private List<ImageVariant> variants;

    private LocalDateTime createdAt;

    public static ImageAsset reconstruct(
            UUID imageAssetId,
            ImageType imageType,
            String masterObjectKey,
            Integer activeRecipeVersion,
            ProcessingStatus processingStatus,
            String processingError,
            LocalDateTime processedAt,
            List<ImageVariant> variants,
            LocalDateTime createdAt
    ) {
        ImageAsset imageAsset = ImageAsset.builder()
                .imageAssetId(requireNonNull(imageAssetId))
                .imageType(requireNonNull(imageType))
                .masterObjectKey(validateObjectKeyIfPresent(masterObjectKey))
                .activeRecipeVersion(requirePositiveIfPresent(activeRecipeVersion))
                .processingStatus(requireNonNull(processingStatus))
                .processingError(processingError)
                .processedAt(processedAt)
                .variants(List.copyOf(requireNonNull(variants)))
                .createdAt(requireNonNull(createdAt))
                .build();

        imageAsset.validateState();

        return imageAsset;
    }

    public static ImageAsset create(UUID imageAssetId, ImageType imageType) {
        return ImageAsset.builder()
                .imageAssetId(requireNonNull(imageAssetId))
                .imageType(requireNonNull(imageType))
                .processingStatus(UPLOADING)
                .variants(List.of())
                .build();
    }

    public boolean isOfType(ImageType imageType) {
        return this.imageType == imageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageAsset that = (ImageAsset) o;

        return this.imageAssetId != null && Objects.equals(this.imageAssetId, that.imageAssetId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateState() {
        validateVariantUniqueness();

        if (this.processingStatus == UPLOADING) {
            validateUploadingState();
        } else if (this.processingStatus == PROCESSING) {
            validateProcessingState();
        } else if (this.processingStatus == READY) {
            validateReadyState();
        } else if (this.processingStatus == FAILED) {
            validateFailedState();
        }
    }

    private void validateUploadingState() {
        if (this.masterObjectKey != null
                || this.activeRecipeVersion != null
                || this.processingError != null
                || this.processedAt != null
                || !this.variants.isEmpty()) {
            throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
        }
    }

    private void validateProcessingState() {
        if (this.masterObjectKey == null
                || this.processingError != null
                || this.processedAt != null) {
            throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
        }
    }

    private void validateReadyState() {
        if (this.masterObjectKey == null
                || this.activeRecipeVersion == null
                || this.processingError != null
                || this.processedAt == null) {
            throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
        }

        boolean activeVariantExists = this.variants.stream()
                .anyMatch(variant -> this.activeRecipeVersion.equals(variant.getRecipeVersion()));

        if (!activeVariantExists) {
            throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
        }
    }

    private void validateFailedState() {
        if (this.processingError == null || this.processingError.isBlank()) {
            throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
        }
    }

    private void validateVariantUniqueness() {
        Set<VariantKey> variantKeys = new HashSet<>();

        for (ImageVariant variant : this.variants) {
            if (!variantKeys.add(new VariantKey(variant.getRecipeVersion(), variant.getVariantType()))) {
                throw new BusinessRuleViolationException(INVALID_IMAGE_ASSET_STATE);
            }
        }
    }

    private static String validateObjectKeyIfPresent(String objectKey) {
        if (objectKey == null) {
            return null;
        }

        if (objectKey.isBlank()) {
            throw new BusinessRuleViolationException(BLANK_VALUE);
        }

        requireMaxLength(objectKey, OBJECT_KEY_MAX_LENGTH);

        return objectKey;
    }

    private record VariantKey(Integer recipeVersion, VariantType variantType) {
    }
}
