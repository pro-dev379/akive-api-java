package net.akive.api.legal.adapter.persistence;

import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentType;
import net.akive.api.support.RepositoryTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.akive.api.legal.domain.LegalDocumentType.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.createLegalDocument;
import static net.akive.api.legal.domain.LegalInteractionType.AGREEMENT;
import static org.assertj.core.api.Assertions.assertThat;

class LegalDocumentJpaAdapterTest extends RepositoryTestSupport {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 10, 29);

    @Autowired
    LegalDocumentRepository legalDocumentRepository;

    @Test
    void 약관_및_정책_문서를_저장한다() {
        LegalDocument legalDocument = createLegalDocument();

        LegalDocument savedLegalDocument = legalDocumentRepository.save(legalDocument);
        flushAndClear();

        LegalDocument foundLegalDocument = legalDocumentRepository.findById(
                savedLegalDocument.getLegalDocumentId()
        ).orElseThrow();

        assertThat(foundLegalDocument.getLegalDocumentId()).isPositive();
        assertThat(foundLegalDocument.getDocumentType()).isEqualTo(legalDocument.getDocumentType());
        assertThat(foundLegalDocument.getVersion()).isEqualTo(legalDocument.getVersion());
        assertThat(foundLegalDocument.getTitle()).isEqualTo(legalDocument.getTitle());
        assertThat(foundLegalDocument.getInteractionType()).isEqualTo(legalDocument.getInteractionType());
        assertThat(foundLegalDocument.isOptional()).isEqualTo(legalDocument.isOptional());
        assertThat(foundLegalDocument.getContent()).isEqualTo(legalDocument.getContent());
        assertThat(foundLegalDocument.getAnnouncedOn()).isEqualTo(legalDocument.getAnnouncedOn());
        assertThat(foundLegalDocument.getEffectiveOn()).isEqualTo(legalDocument.getEffectiveOn());
        assertThat(foundLegalDocument.getExpiresOn()).isEqualTo(legalDocument.getExpiresOn());
        assertThat(foundLegalDocument.getCreatedAt()).isNotNull();
    }

    @Test
    void 약관_및_정책_식별자로_약관_및_정책_문서를_조회한다() {
        LegalDocument savedLegalDocument = legalDocumentRepository.save(createLegalDocument());
        flushAndClear();

        Optional<LegalDocument> foundLegalDocument = legalDocumentRepository.findById(
                savedLegalDocument.getLegalDocumentId()
        );

        assertThat(foundLegalDocument).contains(savedLegalDocument);
    }

    @Test
    void 약관_및_정책_문서_타입이_일치하고_기준일에_유효한_약관_및_정책_문서를_조회한다() {
        LegalDocument effectiveBeforeCurrentDate = saveLegalDocument(
                SERVICE_TERMS,
                "20261028",
                CURRENT_DATE.minusDays(1),
                null
        );
        LegalDocument effectiveOnCurrentDate = saveLegalDocument(
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

    @Test
    void 특정_타입의_시행_약관_및_정책_문서를_최신_시행일순으로_모두_조회한다() {
        LegalDocument recentPastLegalDocument = saveLegalDocument(
                SERVICE_TERMS,
                "20261027",
                CURRENT_DATE.minusDays(2),
                CURRENT_DATE
        );
        LegalDocument oldPastLegalDocument = saveLegalDocument(
                SERVICE_TERMS,
                "20261025",
                CURRENT_DATE.minusDays(4),
                CURRENT_DATE.minusDays(2)
        );
        LegalDocument currentLegalDocument = saveLegalDocument(
                SERVICE_TERMS,
                "20261029",
                CURRENT_DATE,
                null
        );
        saveLegalDocument(
                SERVICE_TERMS,
                "20261030",
                CURRENT_DATE.plusDays(1),
                null
        );
        saveLegalDocument(
                PRIVACY_POLICY,
                "20261027",
                CURRENT_DATE.minusDays(2),
                CURRENT_DATE
        );
        flushAndClear();

        List<LegalDocument> legalDocuments =
                legalDocumentRepository.findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
                        SERVICE_TERMS,
                        CURRENT_DATE
                );

        assertThat(legalDocuments)
                .extracting(LegalDocument::getLegalDocumentId)
                .containsExactly(
                        currentLegalDocument.getLegalDocumentId(),
                        recentPastLegalDocument.getLegalDocumentId(),
                        oldPastLegalDocument.getLegalDocumentId()
                );
    }

    private LegalDocument saveLegalDocument(
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

        return legalDocumentRepository.save(legalDocument);
    }
}
