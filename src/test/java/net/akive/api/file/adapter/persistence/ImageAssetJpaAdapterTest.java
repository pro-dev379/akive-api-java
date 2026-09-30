package net.akive.api.file.adapter.persistence;

import net.akive.api.file.application.required.ImageAssetRepository;
import net.akive.api.file.domain.ImageAsset;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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

        ImageAsset foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getImageType()).isEqualTo(ATTENDANCE_PROOF);
        assertThat(foundImageAsset.getProcessingStatus()).isEqualTo(UPLOADING);
        assertThat(foundImageAsset.getMasterObjectKey()).isNull();
        assertThat(foundImageAsset.getActiveRecipeVersion()).isNull();
        assertThat(foundImageAsset.getProcessingError()).isNull();
        assertThat(foundImageAsset.getProcessedAt()).isNull();
        assertThat(foundImageAsset.getVariants()).isEmpty();
        assertThat(savedImageAsset.getCreatedAt()).isNotNull();
    }

    @Test
    void 기존_이미지_에셋을_다시_저장해도_새_이미지_에셋을_생성하지_않는다() {
        imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        ImageAsset persistedImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        ImageAsset resavedImageAsset = imageAssetRepository.save(persistedImageAsset);
        flushAndClear();

        ImageAsset foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID).orElseThrow();

        assertThat(resavedImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(resavedImageAsset.getCreatedAt()).isEqualTo(persistedImageAsset.getCreatedAt());
        assertThat(foundImageAsset.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(foundImageAsset.getCreatedAt()).isEqualTo(persistedImageAsset.getCreatedAt());
    }

    @Test
    void 식별자로_이미지_에셋을_조회한다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        assertThat(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID))
                .get()
                .extracting(ImageAsset::getImageAssetId)
                .isEqualTo(savedImageAsset.getImageAssetId());
    }

    @Test
    void 이미지_에셋을_삭제한다() {
        ImageAsset savedImageAsset = imageAssetRepository.save(ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF));
        flushAndClear();

        imageAssetRepository.delete(savedImageAsset);
        flushAndClear();

        Optional<ImageAsset> foundImageAsset = imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID);

        assertThat(foundImageAsset).isEmpty();
    }
}
