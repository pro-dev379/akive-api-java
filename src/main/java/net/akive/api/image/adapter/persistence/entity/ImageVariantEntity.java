package net.akive.api.image.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.persistence.BaseTimeEntity;
import net.akive.api.image.adapter.persistence.converter.ImageFormatConverter;
import net.akive.api.image.domain.ImageFormat;
import net.akive.api.image.domain.ImageVariant;
import net.akive.api.image.domain.VariantType;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "image_variant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-image_variant-image_asset_id-recipe_version-variant_type",
                        columnNames = {"image_asset_id", "recipe_version", "variant_type"}
                ),
                @UniqueConstraint(
                        name = "ux-image_variant-object_key",
                        columnNames = "object_key"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ImageVariantEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long imageVariantId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false)
    private ImageAssetEntity imageAsset;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private VariantType variantType;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false)
    private Integer recipeVersion;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false, length = 20)
    @Convert(converter = ImageFormatConverter.class)
    private ImageFormat format;

    private Long byteSize;

    public static ImageVariantEntity from(ImageVariant imageVariant) {
        return new ImageVariantEntity(
                imageVariant.getImageVariantId(),
                null,
                imageVariant.getVariantType(),
                imageVariant.getObjectKey(),
                imageVariant.getRecipeVersion(),
                imageVariant.getWidth(),
                imageVariant.getHeight(),
                imageVariant.getFormat(),
                imageVariant.getByteSize()
        );
    }

    public ImageVariant to() {
        return ImageVariant.reconstruct(
                this.imageVariantId,
                this.variantType,
                this.objectKey,
                this.recipeVersion,
                this.width,
                this.height,
                this.format,
                this.byteSize
        );
    }

    void assignImageAsset(ImageAssetEntity imageAsset) {
        this.imageAsset = imageAsset;
    }
}
