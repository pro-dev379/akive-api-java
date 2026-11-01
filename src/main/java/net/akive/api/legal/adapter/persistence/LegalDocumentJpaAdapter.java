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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class LegalDocumentJpaAdapter implements LegalDocumentRepository {

    private final LegalDocumentJpaRepository legalDocumentJpaRepository;

    @Override
    public LegalDocument save(LegalDocument legalDocument) {
        return legalDocumentJpaRepository.save(LegalDocumentEntity.from(legalDocument)).to();
    }

    @Override
    public Optional<LegalDocument> findById(Long legalDocumentId) {
        return legalDocumentJpaRepository.findById(legalDocumentId)
                .map(LegalDocumentEntity::to);
    }

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

    @Override
    public List<LegalDocument> findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
            LegalDocumentType documentType,
            LocalDate currentDate
    ) {
        return legalDocumentJpaRepository
                .findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(documentType, currentDate)
                .stream()
                .map(LegalDocumentEntity::to)
                .toList();
    }
}
