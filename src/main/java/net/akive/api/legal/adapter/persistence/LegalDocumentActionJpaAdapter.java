package net.akive.api.legal.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.adapter.persistence.entity.LegalDocumentActionEntity;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocumentAction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class LegalDocumentActionJpaAdapter implements LegalDocumentActionRepository {

    private final LegalDocumentActionJpaRepository legalDocumentActionJpaRepository;

    @Override
    public LegalDocumentAction save(LegalDocumentAction legalDocumentAction) {
        return legalDocumentActionJpaRepository.save(LegalDocumentActionEntity.from(legalDocumentAction)).to();
    }

    @Override
    public Optional<LegalDocumentAction> findLatestByUserIdAndLegalDocumentId(Long userId, Long legalDocumentId) {
        return legalDocumentActionJpaRepository.findLatestByUserIdAndLegalDocumentId(userId, legalDocumentId)
                .map(LegalDocumentActionEntity::to);
    }
}
