package net.akive.api.image.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageType;
import net.akive.api.image.domain.ProcessingStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;
import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(
        name = "image_asset",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-image_asset-master_object_key",
                        columnNames = "master_object_key"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ImageAssetEntity extends BaseTimeEntity implements Persistable<UUID> {

    @Id
    @JdbcTypeCode(BINARY)
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID imageAssetId;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ImageType imageType;

    @Column(length = 500)
    private String masterObjectKey;

    private Integer activeRecipeVersion;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ProcessingStatus processingStatus;

    @Column(columnDefinition = "TEXT")
    private String processingError;

    private Instant processedAt;

    @OnDelete(action = CASCADE)
    @OneToMany(mappedBy = "imageAsset", cascade = ALL, orphanRemoval = true)
    private List<ImageVariantEntity> variants = new ArrayList<>();

    public static ImageAssetEntity from(ImageAsset imageAsset) {
        ImageAssetEntity imageAssetEntity = new ImageAssetEntity(
                imageAsset.getImageAssetId(),
                imageAsset.getImageType(),
                imageAsset.getMasterObjectKey(),
                imageAsset.getActiveRecipeVersion(),
                imageAsset.getProcessingStatus(),
                imageAsset.getProcessingError(),
                imageAsset.getProcessedAt(),
                new ArrayList<>()
        );

        imageAsset.getVariants().forEach(imageVariant -> imageAssetEntity.addImageVariant(
                ImageVariantEntity.from(imageVariant)
        ));
        imageAssetEntity.restoreAuditTime(imageAsset.getCreatedAt());

        return imageAssetEntity;
    }

    public ImageAsset to() {
        try {
            return ImageAsset.reconstruct(
                    this.imageAssetId,
                    this.imageType,
                    this.masterObjectKey,
                    this.activeRecipeVersion,
                    this.processingStatus,
                    this.processingError,
                    this.processedAt,
                    this.variants.stream().map(ImageVariantEntity::to).collect(Collectors.toList()),
                    this.getCreatedAt()
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct ImageAsset: " + this.imageAssetId,
                    e
            );
        }
    }

    @Override
    public UUID getId() {
        return this.imageAssetId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.getCreatedAt() == null;
    }

    private void addImageVariant(ImageVariantEntity imageVariant) {
        imageVariant.assignImageAsset(this);

        this.variants.add(imageVariant);
    }
}
