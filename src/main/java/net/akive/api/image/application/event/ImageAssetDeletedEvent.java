package net.akive.api.image.application.event;

import java.util.List;

public record ImageAssetDeletedEvent(List<String> objectKeys) {

    public ImageAssetDeletedEvent {
        objectKeys = List.copyOf(objectKeys);
    }
}
