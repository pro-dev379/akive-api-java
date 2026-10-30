package net.akive.api.legal.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionRecordItem;
import net.akive.api.legal.domain.LegalDocumentActionType;

import java.util.UUID;

public record LegalDocumentActionRecordRequest(
        @NotNull UUID requestId,
        @NotNull @Positive Long legalDocumentId,
        @NotNull LegalDocumentActionType actionType
) {

    public LegalDocumentActionRecordItem to() {
        return new LegalDocumentActionRecordItem(this.requestId, this.legalDocumentId, this.actionType);
    }
}
