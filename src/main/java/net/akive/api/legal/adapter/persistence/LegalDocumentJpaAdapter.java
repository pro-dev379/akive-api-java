package net.akive.api.legal.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.adapter.persistence.entity.LegalDocumentEntity;
import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
class LegalDocumentJpaAdapter implements LegalDocumentRepository {

    private final LegalDocumentJpaRepository legalDocumentJpaRepository;

    @Override
    public List<LegalDocument> findAllCurrentByDocumentTypeIn(
            Collection<LegalDocumentType> documentTypes,
            LocalDate currentDate
    ) {
        return legalDocumentJpaRepository.findAllCurrentByDocumentTypeIn(documentTypes, currentDate)
                .stream()
                .map(LegalDocumentEntity::to)
                .toList();
    }
}
