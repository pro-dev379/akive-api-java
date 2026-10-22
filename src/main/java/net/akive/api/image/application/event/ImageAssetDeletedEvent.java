package net.akive.api.image.application.event;

import net.akive.api.common.domain.DomainValidator;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record ImageAssetDeletedEvent(List<String> objectKeys) {

    public ImageAssetDeletedEvent {
        objectKeys = requireNonNull(objectKeys).stream()
                .map(DomainValidator::requireNotBlank)
                .toList();
    }
}
