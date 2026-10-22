package net.akive.api.image.application.service;

import net.akive.api.image.application.required.ImageAssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static net.akive.api.image.domain.ImageType.ATTENDANCE_PROOF;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.ANOTHER_IMAGE_ASSET_ID;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.IMAGE_ASSET_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UnusedImageAssetCleanupServiceTest {

    @Mock
    ImageAssetRepository imageAssetRepository;

    @Mock
    ImageAssetDeleteService imageAssetDeleteService;

    @InjectMocks
    UnusedImageAssetCleanupService unusedImageAssetCleanupService;

    @Test
    void 이미지_타입별로_미사용_이미지를_조회해_삭제한다() {
        Instant createdBefore = Instant.parse("2026-10-21T00:00:00Z");
        int batchSize = 100;
        given(imageAssetRepository.findUnreferencedAttendanceProofImageAssetIdsCreatedBefore(
                createdBefore,
                batchSize
        )).willReturn(List.of(IMAGE_ASSET_ID));
        given(imageAssetRepository.findUnreferencedProfileImageAssetIdsCreatedBefore(
                createdBefore,
                batchSize
        )).willReturn(List.of(ANOTHER_IMAGE_ASSET_ID));
        given(imageAssetDeleteService.deleteIfUnused(IMAGE_ASSET_ID, ATTENDANCE_PROOF)).willReturn(true);
        given(imageAssetDeleteService.deleteIfUnused(ANOTHER_IMAGE_ASSET_ID, PROFILE_IMAGE)).willReturn(true);

        int deletedCount = unusedImageAssetCleanupService.cleanupCreatedBefore(createdBefore, batchSize);

        assertThat(deletedCount).isEqualTo(2);
        then(imageAssetDeleteService).should().deleteIfUnused(IMAGE_ASSET_ID, ATTENDANCE_PROOF);
        then(imageAssetDeleteService).should().deleteIfUnused(ANOTHER_IMAGE_ASSET_ID, PROFILE_IMAGE);
    }
}
