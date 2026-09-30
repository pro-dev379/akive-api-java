package net.akive.api.file.adapter.persistence;

import net.akive.api.file.adapter.persistence.entity.ImageAssetEntity;
import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.file.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;

class ImageAssetJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    ImageAssetRepository imageAssetRepository;

    @Test
    void 이미지_에셋을_저장한다() {
        ImageAsset imageAsset = ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF);

        ImageAsset savedImageAsset = imageAssetRepository.save(imageAsset);
        flushAndClear();

        ImageAsset foundImageAsset = findImageAsset(IMAGE_ASSET_ID);

        assertThat(savedImageAsset.getCreatedAt()).isNotNull();
        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getImageType()).isEqualTo(ATTENDANCE_PROOF);
        assertThat(foundImageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(foundImageAsset.getMasterObjectKey()).isNull();
        assertThat(foundImageAsset.getActiveRecipeVersion()).isNull();
        assertThat(foundImageAsset.getProcessingError()).isNull();
        assertThat(foundImageAsset.getProcessedAt()).isNull();
        assertThat(foundImageAsset.getVariants()).isEmpty();
        assertThat(foundImageAsset.getCreatedAt()).isEqualTo(savedImageAsset.getCreatedAt());
    }

    @Test
    void 기존_이미지_에셋을_다시_저장해도_새_이미지_에셋을_생성하지_않는다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        ImageAsset resavedImageAsset = imageAssetRepository.save(savedImageAsset);
        flushAndClear();

        ImageAsset foundImageAsset = findImageAsset(IMAGE_ASSET_ID);

        assertThat(resavedImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(resavedImageAsset.getCreatedAt()).isEqualTo(savedImageAsset.getCreatedAt());
        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getCreatedAt()).isEqualTo(savedImageAsset.getCreatedAt());
    }

    private ImageAsset findImageAsset(UUID imageAssetId) {
        return entityManager.find(ImageAssetEntity.class, imageAssetId).to();
    }
}
