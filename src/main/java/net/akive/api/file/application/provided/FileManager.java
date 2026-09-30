package net.akive.api.file.application.provided;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.file.application.provided.dto.UploadUrlCreateCommand;
import net.akive.api.file.application.provided.dto.UploadUrlCreateResult;
import net.akive.api.file.domain.VariantType;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface FileManager {

    UploadUrlCreateResult createUploadUrl(UploadUrlCreateCommand command) throws BusinessRuleViolationException;

    Optional<URI> findImageUrl(UUID imageAssetId, VariantType variantType) throws ResourceNotFoundException;

    void deleteImageAsset(UUID imageAssetId) throws ResourceNotFoundException;
}
