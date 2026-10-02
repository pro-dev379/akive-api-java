package net.akive.api.common.time;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class SystemCurrentTimeProvider implements CurrentTimeProvider {

    private final Clock clock = Clock.systemUTC();

    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}
