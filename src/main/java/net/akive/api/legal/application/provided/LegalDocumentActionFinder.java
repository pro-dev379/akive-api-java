package net.akive.api.legal.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.domain.LegalDocumentAction;

public interface LegalDocumentActionFinder {

    LegalDocumentAction findCurrentOrThrow(Long userId, Long legalDocumentId) throws ResourceNotFoundException;
}
