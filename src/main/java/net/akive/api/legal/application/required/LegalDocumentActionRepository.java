package net.akive.api.legal.application.required;

import net.akive.api.legal.domain.LegalDocumentAction;

import java.util.Optional;
import java.util.UUID;

public interface LegalDocumentActionRepository {

    LegalDocumentAction save(LegalDocumentAction legalDocumentAction);

    Optional<LegalDocumentAction> findByRequestId(UUID requestId);

    Optional<LegalDocumentAction> findLatestByUserIdAndLegalDocumentId(Long userId, Long legalDocumentId);
}
