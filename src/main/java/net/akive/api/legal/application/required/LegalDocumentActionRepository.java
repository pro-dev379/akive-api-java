package net.akive.api.legal.application.required;

import net.akive.api.legal.domain.LegalDocumentAction;

import java.util.Optional;

public interface LegalDocumentActionRepository {

    LegalDocumentAction save(LegalDocumentAction legalDocumentAction);

    Optional<LegalDocumentAction> findLatestByUserIdAndLegalDocumentId(Long userId, Long legalDocumentId);
}
