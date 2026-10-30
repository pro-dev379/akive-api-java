package net.akive.api.legal.adapter.webapi.dto.response;

import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentActionType;

import java.time.Instant;

public record LegalDocumentActionFindResponse(
        Long legalDocumentActionId,
        Long legalDocumentId,
        LegalDocumentActionType actionType,
        LegalDocumentActionContext context,
        Instant actedAt
) {

    public static LegalDocumentActionFindResponse from(LegalDocumentAction legalDocumentAction) {
        return new LegalDocumentActionFindResponse(
                legalDocumentAction.getLegalDocumentActionId(),
                legalDocumentAction.getLegalDocument().getLegalDocumentId(),
                legalDocumentAction.getActionType(),
                legalDocumentAction.getContext(),
                legalDocumentAction.getActedAt()
        );
    }
}
