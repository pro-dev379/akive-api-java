package net.akive.api.common.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FixedCurrentTimeProvider implements CurrentTimeProvider{

    private final Clock clock;

    public FixedCurrentTimeProvider(Instant instant) {
        this.clock = Clock.fixed(instant, ZoneId.of("Asia/Seoul"));
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    @Override
    public Instant instant() {
        return Instant.now(clock);
    }
}
