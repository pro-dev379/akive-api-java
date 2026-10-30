package net.akive.api.legal.application.required;

import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LegalDocumentRepository {

    List<LegalDocument> findAllCurrentByDocumentTypeIn(
            Collection<LegalDocumentType> documentTypes,
            LocalDate currentDate
    );

    List<Long> findAllCurrentIdsByDocumentType(
            LegalDocumentType documentType,
            LocalDate currentDate
    );
}
