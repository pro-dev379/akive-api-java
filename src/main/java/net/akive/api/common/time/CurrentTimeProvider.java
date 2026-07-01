package net.akive.api.common.time;

import java.time.Instant;
import java.time.LocalDateTime;

public interface CurrentTimeProvider {

    LocalDateTime now();
    Instant instant();
}
