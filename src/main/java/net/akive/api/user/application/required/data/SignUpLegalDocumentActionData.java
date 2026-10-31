package net.akive.api.user.application.required.data;

import net.akive.api.user.domain.SignUpLegalDocumentActionType;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record SignUpLegalDocumentActionData(
        UUID requestId,
        Long legalDocumentId,
        SignUpLegalDocumentActionType actionType
) {

    public SignUpLegalDocumentActionData {
        requireNonNull(requestId);
        requirePositive(legalDocumentId);
        requireNonNull(actionType);
    }
}
