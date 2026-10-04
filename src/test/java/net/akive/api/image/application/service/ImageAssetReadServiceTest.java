package net.akive.api.image.application.service;

import net.akive.api.core.config.aws.CdnProperties;
import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.image.domain.ImageVariant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;

import static net.akive.api.core.config.aws.fixture.CdnFixture.CDN_BASE_URL;
import static net.akive.api.image.domain.VariantType.DETAIL;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.*;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageAssetReadServiceTest {

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    CdnProperties cdnProperties;

    @InjectMocks
    ImageAssetReadService imageAssetReadService;

    @Test
    void 이미지_URL을_조회한다() {
        ImageVariant activeImageVariant = detailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageVariant inactiveImageVariant = detailImageVariant(SECOND_IMAGE_VARIANT_ID, ANOTHER_RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(activeImageVariant, inactiveImageVariant);
        URI expectedUrl = cdnUrlOf(activeImageVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));
        given(cdnProperties.resolve(activeImageVariant.getObjectKey())).willReturn(expectedUrl);

        Optional<URI> result = imageAssetReadService.findImageUrl(IMAGE_ASSET_ID, DETAIL);

        assertThat(result).contains(expectedUrl);
        then(cdnProperties).should().resolve(activeImageVariant.getObjectKey());
        then(cdnProperties).shouldHaveNoMoreInteractions();
    }

    @Test
    void 활성화된_변환_규칙_버전의_변환본이_없으면_이미지_URL을_조회할_수_없다() {
        ImageVariant activeThumbnailVariant = thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(activeThumbnailVariant);

        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        Optional<URI> result = imageAssetReadService.findImageUrl(IMAGE_ASSET_ID, DETAIL);

        assertThat(result).isEmpty();
        then(cdnProperties).shouldHaveNoInteractions();
    }

    private static URI cdnUrlOf(ImageVariant variant) {
        return URI.create(CDN_BASE_URL + "/" + variant.getObjectKey());
    }
}
