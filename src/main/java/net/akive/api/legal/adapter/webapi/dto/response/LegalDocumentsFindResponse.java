package net.akive.api.legal.adapter.webapi.dto.response;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalInteractionType;

import java.time.LocalDate;
import java.util.List;

public record LegalDocumentsFindResponse(List<LegalDocumentResponse> legalDocuments) {

    public static LegalDocumentsFindResponse from(List<LegalDocument> legalDocuments) {
        return new LegalDocumentsFindResponse(
                legalDocuments.stream().map(LegalDocumentResponse::from).toList()
        );
    }

    public record LegalDocumentResponse(
            Long legalDocumentId,
            LegalDocumentType documentType,
            String version,
            String title,
            LegalInteractionType interactionType,
            boolean isOptional,
            String content,
            LocalDate announcedOn,
            LocalDate effectiveOn,
            LocalDate expiresOn
    ) {

        public static LegalDocumentResponse from(LegalDocument legalDocument) {
            return new LegalDocumentResponse(
                    legalDocument.getLegalDocumentId(),
                    legalDocument.getDocumentType(),
                    legalDocument.getVersion(),
                    legalDocument.getTitle(),
                    legalDocument.getInteractionType(),
                    legalDocument.isOptional(),
                    legalDocument.getContent(),
                    legalDocument.getAnnouncedOn(),
                    legalDocument.getEffectiveOn(),
                    legalDocument.getExpiresOn()
            );
        }
    }
}
