package net.akive.api.image.adapter.generation;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import net.akive.api.image.application.required.ImageAssetIdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class UuidV7ImageAssetIdGenerator implements ImageAssetIdGenerator {

    private final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    @Override
    public UUID generate() {
        return generator.generate();
    }
}
