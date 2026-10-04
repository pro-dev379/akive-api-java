package net.akive.api.image.adapter.webapi.mapper;

import net.akive.api.core.config.aws.CdnProperties;
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
import static net.akive.api.image.domain.fixture.ImageAssetFixture.RECIPE_VERSION;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.readyImageAsset;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.FIRST_IMAGE_VARIANT_ID;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.detailImageVariant;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.thumbnailImageVariant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageResponseMapperTest {

    @Mock
    CdnProperties cdnProperties;

    @InjectMocks
    ImageResponseMapper imageResponseMapper;

    @Test
    void 활성화된_이미지_변환본의_URL을_생성한다() {
        ImageVariant activeVariant = detailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION);
        ImageAsset imageAsset = readyImageAsset(activeVariant);
        URI expectedUrl = URI.create(CDN_BASE_URL + "/" + activeVariant.getObjectKey());
        given(cdnProperties.resolve(activeVariant.getObjectKey())).willReturn(expectedUrl);

        Optional<URI> result = imageResponseMapper.resolveUrl(imageAsset, DETAIL);

        assertThat(result).contains(expectedUrl);
        then(cdnProperties).should().resolve(activeVariant.getObjectKey());
    }

    @Test
    void 요청한_유형의_활성화된_변환본이_없으면_URL을_생성하지_않는다() {
        ImageAsset imageAsset = readyImageAsset(thumbnailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION));

        Optional<URI> result = imageResponseMapper.resolveUrl(imageAsset, DETAIL);

        assertThat(result).isEmpty();
        then(cdnProperties).shouldHaveNoInteractions();
    }
}
