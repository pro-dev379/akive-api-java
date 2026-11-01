package net.akive.api.legal.adapter.webapi.dto.response;

import net.akive.api.legal.application.provided.dto.LegalDocumentActionStatusResult;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalInteractionType;

import java.time.LocalDate;
import java.util.List;

public record LegalDocumentActionStatusesFindResponse(
        List<LegalDocumentActionStatusResponse> legalDocumentActionStatuses
) {

    public static LegalDocumentActionStatusesFindResponse from(
            List<LegalDocumentActionStatusResult> legalDocumentActionStatusResults
    ) {
        return new LegalDocumentActionStatusesFindResponse(
                legalDocumentActionStatusResults.stream().map(LegalDocumentActionStatusResponse::from).toList()
        );
    }

    public record LegalDocumentActionStatusResponse(
            Long legalDocumentId,
            LegalDocumentType documentType,
            String version,
            String title,
            LegalInteractionType interactionType,
            boolean isOptional,
            LocalDate effectiveOn,
            LocalDate expiresOn,
            LegalDocumentActionFindResponse currentAction
    ) {

        public static LegalDocumentActionStatusResponse from(LegalDocumentActionStatusResult result) {
            LegalDocument legalDocument = result.legalDocument();

            return new LegalDocumentActionStatusResponse(
                    legalDocument.getLegalDocumentId(),
                    legalDocument.getDocumentType(),
                    legalDocument.getVersion(),
                    legalDocument.getTitle(),
                    legalDocument.getInteractionType(),
                    legalDocument.isOptional(),
                    legalDocument.getEffectiveOn(),
                    legalDocument.getExpiresOn(),
                    result.currentAction().map(LegalDocumentActionFindResponse::from).orElse(null)
            );
        }
    }
}
