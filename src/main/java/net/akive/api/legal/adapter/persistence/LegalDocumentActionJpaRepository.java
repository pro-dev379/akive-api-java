package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.adapter.persistence.entity.LegalDocumentActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface LegalDocumentActionJpaRepository extends JpaRepository<LegalDocumentActionEntity, Long> {

    @Query("""
        select legalDocumentAction
        from LegalDocumentActionEntity legalDocumentAction
        join fetch legalDocumentAction.user
        join fetch legalDocumentAction.legalDocument
        where legalDocumentAction.user.userId = :userId
          and legalDocumentAction.legalDocument.legalDocumentId = :legalDocumentId
        order by legalDocumentAction.actedAt desc, legalDocumentAction.legalDocumentActionId desc
        limit 1
    """)
    Optional<LegalDocumentActionEntity> findLatestByUserIdAndLegalDocumentId(Long userId, Long legalDocumentId);
}
