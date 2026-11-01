package net.akive.api.legal.application.provided.dto;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;

import java.util.Optional;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record LegalDocumentActionStatusResult(
        LegalDocument legalDocument,
        Optional<LegalDocumentAction> currentAction
) {

    public LegalDocumentActionStatusResult {
        requireNonNull(legalDocument);
        requireNonNull(currentAction);
    }
}
