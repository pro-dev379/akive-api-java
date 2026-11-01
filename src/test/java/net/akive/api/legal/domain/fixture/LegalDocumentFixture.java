package net.akive.api.legal.domain.fixture;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalInteractionType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static net.akive.api.legal.domain.LegalDocumentType.SERVICE_TERMS;
import static net.akive.api.legal.domain.LegalInteractionType.AGREEMENT;

public final class LegalDocumentFixture {

    public static final Long LEGAL_DOCUMENT_ID = 1L;
    public static final Long ANOTHER_LEGAL_DOCUMENT_ID = 2L;
    public static final Long UNKNOWN_LEGAL_DOCUMENT_ID = Long.MAX_VALUE;
    public static final String VERSION = "20261028";
    public static final String TITLE = "서비스 이용약관";
    public static final String CONTENT = "약관 내용";
    public static final LocalDate ANNOUNCED_ON = LocalDate.of(2026, 10, 29);
    public static final LocalDate EFFECTIVE_ON = LocalDate.of(2026, 10, 29);
    public static final LocalDate EXPIRES_ON = LocalDate.of(2026, 10, 30);
    public static final Instant CREATED_AT = Instant.parse("2026-10-28T12:00:00Z");

    private LegalDocumentFixture() {
    }

    public static LegalDocument createLegalDocument() {
        return createLegalDocument(SERVICE_TERMS, TITLE);
    }

    public static LegalDocument createLegalDocument(LegalDocumentType documentType) {
        return createLegalDocument(documentType, documentType.name());
    }

    private static LegalDocument createLegalDocument(
            LegalDocumentType documentType,
            String title
    ) {
        return LegalDocument.create(
                documentType,
                VERSION,
                title,
                AGREEMENT,
                false,
                CONTENT,
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                null
        );
    }

    public static LegalDocument reconstructLegalDocument() {
        return reconstructLegalDocument(LEGAL_DOCUMENT_ID);
    }

    public static LegalDocument reconstructLegalDocument(Long legalDocumentId) {
        return reconstructLegalDocument(
                legalDocumentId,
                SERVICE_TERMS,
                TITLE,
                AGREEMENT,
                null
        );
    }

    public static LegalDocument reconstructLegalDocument(
            Long legalDocumentId,
            LegalDocumentType documentType
    ) {
        return reconstructLegalDocument(
                legalDocumentId,
                documentType,
                documentType.name(),
                AGREEMENT,
                null
        );
    }

    public static LegalDocument reconstructLegalDocument(LegalInteractionType interactionType) {
        return reconstructLegalDocument(
                LEGAL_DOCUMENT_ID,
                SERVICE_TERMS,
                TITLE,
                interactionType,
                null
        );
    }

    public static LegalDocument reconstructLegalDocument(
            Long legalDocumentId,
            LegalDocumentType documentType,
            LegalInteractionType interactionType
    ) {
        return reconstructLegalDocument(
                legalDocumentId,
                documentType,
                documentType.name(),
                interactionType,
                null
        );
    }

    public static LegalDocument reconstructExpiredLegalDocument() {
        return reconstructLegalDocument(
                LEGAL_DOCUMENT_ID,
                SERVICE_TERMS,
                TITLE,
                AGREEMENT,
                EXPIRES_ON
        );
    }

    public static List<LegalDocument> reconstructLegalDocuments(List<LegalDocumentType> documentTypes) {
        return IntStream.range(0, documentTypes.size())
                .mapToObj(index -> reconstructLegalDocument(index + 1L, documentTypes.get(index)))
                .toList();
    }

    private static LegalDocument reconstructLegalDocument(
            Long legalDocumentId,
            LegalDocumentType documentType,
            String title,
            LegalInteractionType interactionType,
            LocalDate expiresOn
    ) {
        return LegalDocument.reconstruct(
                legalDocumentId,
                documentType,
                VERSION,
                title,
                interactionType,
                false,
                CONTENT,
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                expiresOn,
                CREATED_AT
        );
    }
}
