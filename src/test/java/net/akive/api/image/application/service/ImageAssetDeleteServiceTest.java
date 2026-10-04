package net.akive.api.image.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.image.application.required.ImageStorage;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageVariant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.*;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ImageAssetDeleteServiceTest {

    @Mock
    ImageStorage imageStorage;

    @Mock
    ImageAssetRepository imageAssetRepository;

    @InjectMocks
    ImageAssetDeleteService imageAssetDeleteService;

    @Test
    void 이미지_에셋을_삭제한다() {
        ImageVariant thumbnailVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageVariant detailVariant = detailImageVariant(SECOND_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(thumbnailVariant, detailVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        imageAssetDeleteService.delete(IMAGE_ASSET_ID);

        then(imageStorage).should().delete(MASTER_OBJECT_KEY);
        then(imageStorage).should().delete(thumbnailVariant.getObjectKey());
        then(imageStorage).should().delete(detailVariant.getObjectKey());
        then(imageAssetRepository).should().delete(imageAsset);
    }

    @Test
    void 존재하지_않는_이미지_에셋은_삭제할_수_없다() {
        given(imageAssetRepository.findByImageAssetId(UNKNOWN_IMAGE_ASSET_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> imageAssetDeleteService.delete(UNKNOWN_IMAGE_ASSET_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(IMAGE_ASSET_NOT_FOUND);

        then(imageAssetRepository).should().findByImageAssetId(UNKNOWN_IMAGE_ASSET_ID);
        then(imageStorage).shouldHaveNoInteractions();
        then(imageAssetRepository).should(never()).delete(any(ImageAsset.class));
    }
}
