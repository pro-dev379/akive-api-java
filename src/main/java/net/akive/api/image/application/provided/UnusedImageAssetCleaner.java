package net.akive.api.image.application.provided;

import java.time.Instant;

public interface UnusedImageAssetCleaner {

    int cleanupCreatedBefore(Instant createdBefore, int batchSize);
}
