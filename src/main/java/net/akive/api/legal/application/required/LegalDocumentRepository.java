package net.akive.api.legal.application.required;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LegalDocumentRepository {

    LegalDocument save(LegalDocument legalDocument);

    Optional<LegalDocument> findById(Long legalDocumentId);

    List<LegalDocument> findAllCurrentByDocumentTypeIn(
            Collection<LegalDocumentType> documentTypes,
            LocalDate currentDate
    );

    List<Long> findAllCurrentIdsByDocumentType(
            LegalDocumentType documentType,
            LocalDate currentDate
    );
}
