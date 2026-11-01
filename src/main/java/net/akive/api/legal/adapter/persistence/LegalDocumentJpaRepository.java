package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.adapter.persistence.entity.LegalDocumentEntity;
import net.akive.api.legal.domain.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

interface LegalDocumentJpaRepository extends JpaRepository<LegalDocumentEntity, Long> {

    @Query("""
        select legalDocument
        from LegalDocumentEntity legalDocument
        where legalDocument.documentType in :documentTypes
          and legalDocument.effectiveOn <= :currentDate
          and (legalDocument.expiresOn is null or legalDocument.expiresOn > :currentDate)
    """)
    List<LegalDocumentEntity> findAllCurrentByDocumentTypeIn(
            Collection<LegalDocumentType> documentTypes,
            LocalDate currentDate
    );

    @Query("""
        select legalDocument
        from LegalDocumentEntity legalDocument
        where legalDocument.documentType = :documentType
          and legalDocument.effectiveOn <= :currentDate
        order by legalDocument.effectiveOn desc, legalDocument.legalDocumentId desc
    """)
    List<LegalDocumentEntity> findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
            LegalDocumentType documentType,
            LocalDate currentDate
    );
}
