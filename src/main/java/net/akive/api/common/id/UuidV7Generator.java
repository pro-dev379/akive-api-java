package net.akive.api.common.id;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class UuidV7Generator implements UuidGenerator {

    private final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    @Override
    public UUID generate() {
        return generator.generate();
    }
}
