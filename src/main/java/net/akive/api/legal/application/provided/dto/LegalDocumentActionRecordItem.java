package net.akive.api.legal.application.provided.dto;

import net.akive.api.legal.domain.LegalDocumentActionType;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record LegalDocumentActionRecordItem(
        UUID requestId,
        Long legalDocumentId,
        LegalDocumentActionType actionType
) {

    public LegalDocumentActionRecordItem {
        requireNonNull(requestId);
        requirePositive(legalDocumentId);
        requireNonNull(actionType);
    }
}
