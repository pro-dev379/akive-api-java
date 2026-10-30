package net.akive.api.legal.application.provided;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;

import java.util.List;

public interface LegalDocumentFinder {

    List<LegalDocument> findAllCurrentByContext(LegalDocumentActionContext context);

    Long findCurrentIdByDocumentType(LegalDocumentType documentType);
}
