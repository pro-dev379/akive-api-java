package net.akive.api.file.application.service;

import net.akive.api.file.application.required.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.file.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.file.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageAssetUploadCheckServiceTest {

    @Mock
    FileStorage fileStorage;

    @InjectMocks
    ImageAssetUploadCheckService imageAssetUploadCheckService;

    @Test
    void 원본_이미지의_업로드_여부를_확인한다() {
        given(fileStorage.existsOriginalImage(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(true);

        boolean uploaded = imageAssetUploadCheckService.isOriginalUploaded(IMAGE_ASSET_ID, ATTENDANCE_PROOF);

        assertThat(uploaded).isTrue();

        then(fileStorage).should().existsOriginalImage(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
    }
}
