package net.akive.api.common.time;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;

public class FixedCurrentTimeProvider implements CurrentTimeProvider {

    private final Clock clock;

    public FixedCurrentTimeProvider(Instant instant) {
        this.clock = Clock.fixed(instant, UTC);
    }

    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}
