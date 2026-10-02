package net.akive.api.common.time;

import java.time.Instant;

public interface CurrentTimeProvider {

    Instant now();
}
