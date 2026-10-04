package net.akive.api.image.application.service;

import net.akive.api.image.application.required.ImageAssetRepository;
import net.akive.api.image.domain.ImageAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageAssetReadServiceTest {

    @Mock
    ImageAssetRepository imageAssetRepository;

    @InjectMocks
    ImageAssetReadService imageAssetReadService;

    @Test
    void 이미지_에셋을_조회한다() {
        ImageAsset imageAsset = ImageAsset.create(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
        given(imageAssetRepository.findByImageAssetId(IMAGE_ASSET_ID)).willReturn(Optional.of(imageAsset));

        Optional<ImageAsset> result = imageAssetReadService.find(IMAGE_ASSET_ID);

        assertThat(result).contains(imageAsset);
        then(imageAssetRepository).should().findByImageAssetId(IMAGE_ASSET_ID);
    }
}
