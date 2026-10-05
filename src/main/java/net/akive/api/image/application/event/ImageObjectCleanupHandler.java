package net.akive.api.image.application.event;

import lombok.RequiredArgsConstructor;
import net.akive.api.image.application.required.ImageStorage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
class ImageObjectCleanupHandler {

    private final ImageStorage imageStorage;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void handle(ImageAssetDeletedEvent imageAssetDeletedEvent) {
        imageAssetDeletedEvent.objectKeys().forEach(imageStorage::delete);
    }
}
