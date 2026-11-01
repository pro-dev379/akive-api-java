package net.akive.api.legal.application.provided;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;

import java.util.List;

public interface LegalDocumentFinder {

    LegalDocument findOrThrow(Long legalDocumentId) throws ResourceNotFoundException;

    LegalDocument findCurrentByDocumentType(LegalDocumentType documentType) throws ResourceNotFoundException;

    List<LegalDocument> findAllCurrentByContext(LegalDocumentActionContext context);

    List<LegalDocument> findAllHistoryByDocumentType(LegalDocumentType documentType);
}
