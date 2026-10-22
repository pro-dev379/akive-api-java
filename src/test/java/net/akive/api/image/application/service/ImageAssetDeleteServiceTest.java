package net.akive.api.image.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.image.application.event.ImageAssetDeletedEvent;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageVariant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.*;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ImageAssetDeleteServiceTest {

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    ImageAssetDeleteService imageAssetDeleteService;

    @Test
    void 이미지_에셋을_삭제한다() {
        ImageVariant thumbnailVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageVariant detailVariant = detailImageVariant(SECOND_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(thumbnailVariant, detailVariant);

        given(imageAssetRepository.findByImageAssetIdForUpdate(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        imageAssetDeleteService.delete(IMAGE_ASSET_ID);

        then(imageAssetRepository).should().delete(imageAsset);
        ArgumentCaptor<ImageAssetDeletedEvent> eventCaptor = ArgumentCaptor.forClass(ImageAssetDeletedEvent.class);
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().objectKeys()).containsExactly(
                MASTER_OBJECT_KEY,
                thumbnailVariant.getObjectKey(),
                detailVariant.getObjectKey()
        );
    }

    @Test
    void 존재하지_않는_이미지_에셋은_삭제할_수_없다() {
        given(imageAssetRepository.findByImageAssetIdForUpdate(UNKNOWN_IMAGE_ASSET_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> imageAssetDeleteService.delete(UNKNOWN_IMAGE_ASSET_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(IMAGE_ASSET_NOT_FOUND);

        then(imageAssetRepository).should().findByImageAssetIdForUpdate(UNKNOWN_IMAGE_ASSET_ID);
        then(applicationEventPublisher).shouldHaveNoInteractions();
        then(imageAssetRepository).should(never()).delete(any(ImageAsset.class));
    }
}
