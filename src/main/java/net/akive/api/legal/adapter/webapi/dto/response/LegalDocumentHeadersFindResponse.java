package net.akive.api.legal.adapter.webapi.dto.response;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalInteractionType;

import java.time.LocalDate;
import java.util.List;

public record LegalDocumentHeadersFindResponse(
        List<LegalDocumentHeaderResponse> legalDocumentHeaders
) {

    public static LegalDocumentHeadersFindResponse from(List<LegalDocument> legalDocuments) {
        return new LegalDocumentHeadersFindResponse(
                legalDocuments.stream()
                        .map(LegalDocumentHeaderResponse::from)
                        .toList()
        );
    }

    public record LegalDocumentHeaderResponse(
            Long legalDocumentId,
            LegalDocumentType documentType,
            String version,
            String title,
            LegalInteractionType interactionType,
            boolean isOptional,
            LocalDate effectiveOn,
            LocalDate expiresOn
    ) {

        public static LegalDocumentHeaderResponse from(LegalDocument legalDocument) {
            return new LegalDocumentHeaderResponse(
                    legalDocument.getLegalDocumentId(),
                    legalDocument.getDocumentType(),
                    legalDocument.getVersion(),
                    legalDocument.getTitle(),
                    legalDocument.getInteractionType(),
                    legalDocument.isOptional(),
                    legalDocument.getEffectiveOn(),
                    legalDocument.getExpiresOn()
            );
        }
    }
}
