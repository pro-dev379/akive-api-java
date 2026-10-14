package net.akive.api.eventprocessing.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProducedEventPolicy {

    public static final int PUBLISH_BATCH_SIZE = 50;
    public static final int PUBLISH_ATTEMPT_MAX_COUNT = 5;

    public static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);
    public static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(5);
    public static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(5);
}
