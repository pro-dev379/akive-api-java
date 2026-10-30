package net.akive.api.legal.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.legal.domain.LegalPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static net.akive.api.common.exception.business.ResourceNotFoundError.CURRENT_LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.common.time.ServiceDayPolicy.toKoreanDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class LegalDocumentQueryService implements LegalDocumentFinder {

    private final LegalDocumentRepository legalDocumentRepository;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public LegalDocument findOrThrow(Long legalDocumentId) throws ResourceNotFoundException {
        return legalDocumentRepository.findById(legalDocumentId)
                .orElseThrow(() -> new ResourceNotFoundException(LEGAL_DOCUMENT_NOT_FOUND));
    }

    @Override
    public List<LegalDocument> findAllCurrentByContext(LegalDocumentActionContext context) {
        List<LegalDocumentType> documentTypes = LegalPolicy.documentTypesFor(context);
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());
        List<LegalDocument> legalDocuments = legalDocumentRepository.findAllCurrentByDocumentTypeIn(
                documentTypes,
                currentDate
        );

        Map<LegalDocumentType, LegalDocument> legalDocumentByType = toDocumentByType(legalDocuments);

        return documentTypes.stream()
                .map(documentType -> findRequiredDocument(legalDocumentByType, documentType, context))
                .toList();
    }

    @Override
    public List<LegalDocument> findAllHistoryByDocumentType(LegalDocumentType documentType) {
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());

        return legalDocumentRepository.findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
                documentType,
                currentDate
        );
    }

    @Override
    public Long findCurrentIdByDocumentType(LegalDocumentType documentType) throws ResourceNotFoundException {
        LocalDate currentDate = toKoreanDate(currentTimeProvider.now());
        List<Long> legalDocumentIds = legalDocumentRepository.findAllCurrentIdsByDocumentType(
                documentType,
                currentDate
        );

        if (legalDocumentIds.isEmpty()) {
            throw new ResourceNotFoundException(CURRENT_LEGAL_DOCUMENT_NOT_FOUND);
        }

        if (legalDocumentIds.size() > 1) {
            throw new IllegalStateException(
                    "Multiple current legal documents found for type: " + documentType
            );
        }

        return legalDocumentIds.getFirst();
    }

    private Map<LegalDocumentType, LegalDocument> toDocumentByType(List<LegalDocument> legalDocuments) {
        Map<LegalDocumentType, LegalDocument> legalDocumentByType = new EnumMap<>(LegalDocumentType.class);

        for (LegalDocument legalDocument : legalDocuments) {
            LegalDocument previous = legalDocumentByType.put(legalDocument.getDocumentType(), legalDocument);

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple current legal documents found for type: " + legalDocument.getDocumentType()
                );
            }
        }

        return legalDocumentByType;
    }

    private LegalDocument findRequiredDocument(
            Map<LegalDocumentType, LegalDocument> legalDocumentByType,
            LegalDocumentType documentType,
            LegalDocumentActionContext context
    ) {
        LegalDocument legalDocument = legalDocumentByType.get(documentType);

        if (legalDocument == null) {
            throw new IllegalStateException(
                    "Current legal document not found: context=" + context + ", documentType=" + documentType
            );
        }

        return legalDocument;
    }
}
