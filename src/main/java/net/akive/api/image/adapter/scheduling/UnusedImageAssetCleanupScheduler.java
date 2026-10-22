package net.akive.api.image.adapter.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.image.application.provided.UnusedImageAssetCleaner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
class UnusedImageAssetCleanupScheduler {

    private final UnusedImageAssetCleaner unusedImageAssetCleaner;
    private final ImageCleanupProperties imageCleanupProperties;
    private final CurrentTimeProvider currentTimeProvider;

    @Scheduled(cron = "${image.cleanup.cron}")
    void cleanup() {
        Instant createdBefore = currentTimeProvider.now().minus(imageCleanupProperties.retention());
        int deletedCount = unusedImageAssetCleaner.cleanupCreatedBefore(
                createdBefore,
                imageCleanupProperties.batchSize()
        );

        if (deletedCount > 0) {
            log.info("Deleted unused image assets: count={}, createdBefore={}", deletedCount, createdBefore);
        }
    }
}
