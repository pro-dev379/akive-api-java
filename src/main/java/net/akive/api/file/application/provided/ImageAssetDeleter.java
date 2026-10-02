package net.akive.api.file.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;

import java.util.UUID;

public interface ImageAssetDeleter {

    void delete(UUID imageAssetId) throws ResourceNotFoundException;
}
