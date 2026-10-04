package net.akive.api.image.application.service;

import net.akive.api.image.application.required.ImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageAssetUploadCheckServiceTest {

    @Mock
    ImageStorage imageStorage;

    @InjectMocks
    ImageAssetUploadCheckService imageAssetUploadCheckService;

    @Test
    void 원본_이미지의_업로드_여부를_확인한다() {
        given(imageStorage.existsOriginalImage(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(true);

        boolean uploaded = imageAssetUploadCheckService.isOriginalImageUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF);

        assertThat(uploaded).isTrue();

        then(imageStorage).should().existsOriginalImage(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
    }
}
