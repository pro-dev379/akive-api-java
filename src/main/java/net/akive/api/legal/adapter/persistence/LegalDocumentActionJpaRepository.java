package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.adapter.persistence.entity.LegalDocumentActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface LegalDocumentActionJpaRepository extends JpaRepository<LegalDocumentActionEntity, Long> {

    @Query("""
        select legalDocumentAction
        from LegalDocumentActionEntity legalDocumentAction
        join fetch legalDocumentAction.user
        join fetch legalDocumentAction.legalDocument
        where legalDocumentAction.requestId = :requestId
    """)
    Optional<LegalDocumentActionEntity> findByRequestId(UUID requestId);

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

    @Query("""
        select legalDocumentAction
        from LegalDocumentActionEntity legalDocumentAction
        join fetch legalDocumentAction.user
        join fetch legalDocumentAction.legalDocument
        where legalDocumentAction.user.userId = :userId
          and legalDocumentAction.legalDocument.legalDocumentId in :legalDocumentIds
          and not exists (
              select newerLegalDocumentAction.legalDocumentActionId
              from LegalDocumentActionEntity newerLegalDocumentAction
              where newerLegalDocumentAction.user.userId = :userId
                and newerLegalDocumentAction.legalDocument.legalDocumentId =
                    legalDocumentAction.legalDocument.legalDocumentId
                and (
                    newerLegalDocumentAction.actedAt > legalDocumentAction.actedAt
                    or (
                        newerLegalDocumentAction.actedAt = legalDocumentAction.actedAt
                        and newerLegalDocumentAction.legalDocumentActionId >
                            legalDocumentAction.legalDocumentActionId
                    )
                )
          )
    """)
    List<LegalDocumentActionEntity> findAllLatestByUserIdAndLegalDocumentIdIn(
            Long userId,
            Collection<Long> legalDocumentIds
    );
}
