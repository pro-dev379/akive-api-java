package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.adapter.persistence.entity.LegalDocumentEntity;
import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static net.akive.api.legal.domain.LegalDocumentType.*;
import static net.akive.api.legal.domain.LegalInteractionType.AGREEMENT;
import static org.assertj.core.api.Assertions.assertThat;

class LegalDocumentJpaAdapterTest extends RepositoryTestSupport {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 10, 29);

    @Autowired
    LegalDocumentRepository legalDocumentRepository;

    @Test
    void 약관_및_정책_문서_유형이_일치하고_기준일에_유효한_약관_및_정책_문서를_조회한다() {
        LegalDocumentEntity effectiveBeforeCurrentDate = saveLegalDocument(
                SERVICE_TERMS,
                "20261028",
                CURRENT_DATE.minusDays(1),
                null
        );
        LegalDocumentEntity effectiveOnCurrentDate = saveLegalDocument(
                PRIVACY_POLICY,
                "20261029",
                CURRENT_DATE,
                CURRENT_DATE.plusDays(1)
        );
        saveLegalDocument(
                SERVICE_TERMS,
                "20261030",
                CURRENT_DATE.plusDays(1),
                null
        );
        saveLegalDocument(
                SERVICE_TERMS,
                "20261027",
                CURRENT_DATE.minusDays(2),
                CURRENT_DATE
        );
        saveLegalDocument(
                AI_LIMITATIONS,
                "20261029",
                CURRENT_DATE,
                null
        );
        flushAndClear();

        List<LegalDocument> legalDocuments = legalDocumentRepository.findAllCurrentByDocumentTypeIn(
                Set.of(SERVICE_TERMS, PRIVACY_POLICY),
                CURRENT_DATE
        );

        assertThat(legalDocuments)
                .extracting(LegalDocument::getLegalDocumentId)
                .containsExactlyInAnyOrder(
                        effectiveBeforeCurrentDate.getLegalDocumentId(),
                        effectiveOnCurrentDate.getLegalDocumentId()
                );
    }

    private LegalDocumentEntity saveLegalDocument(
            LegalDocumentType documentType,
            String version,
            LocalDate effectiveOn,
            LocalDate expiresOn
    ) {
        LegalDocument legalDocument = LegalDocument.create(
                documentType,
                version,
                documentType.name(),
                AGREEMENT,
                false,
                "약관 및 정책 내용",
                effectiveOn,
                effectiveOn,
                expiresOn
        );
        LegalDocumentEntity legalDocumentEntity = LegalDocumentEntity.from(legalDocument);
        entityManager.persist(legalDocumentEntity);

        return legalDocumentEntity;
    }
}
