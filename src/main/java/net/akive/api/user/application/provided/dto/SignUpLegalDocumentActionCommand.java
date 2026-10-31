package net.akive.api.user.application.provided.dto;

import net.akive.api.user.domain.SignUpLegalDocumentActionType;

import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record SignUpLegalDocumentActionCommand(
        UUID requestId,
        Long legalDocumentId,
        SignUpLegalDocumentActionType actionType
) {

    public SignUpLegalDocumentActionCommand {
        requireNonNull(requestId);
        requirePositive(legalDocumentId);
        requireNonNull(actionType);
    }
}
