package net.akive.api.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class SystemCurrentTimeProvider implements CurrentTimeProvider{

    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    @Override
    public Instant instant() {
        return Instant.now(clock);
    }
}
