package net.akive.api.image.application.event;

import net.akive.api.image.application.required.ImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static net.akive.api.image.domain.fixture.ImageAssetFixture.MASTER_OBJECT_KEY;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.DETAIL_OBJECT_KEY;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.THUMBNAIL_OBJECT_KEY;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ImageObjectCleanupHandlerTest {

    @Mock
    ImageStorage imageStorage;

    @InjectMocks
    ImageObjectCleanupHandler imageObjectCleanupHandler;

    @Test
    void 이미지_에셋_삭제_이벤트를_받으면_스토리지에서_이미지_오브젝트를_삭제한다() {
        ImageAssetDeletedEvent event = new ImageAssetDeletedEvent(List.of(
                MASTER_OBJECT_KEY,
                THUMBNAIL_OBJECT_KEY,
                DETAIL_OBJECT_KEY
        ));

        imageObjectCleanupHandler.handle(event);

        then(imageStorage).should().delete(MASTER_OBJECT_KEY);
        then(imageStorage).should().delete(THUMBNAIL_OBJECT_KEY);
        then(imageStorage).should().delete(DETAIL_OBJECT_KEY);
    }
}
