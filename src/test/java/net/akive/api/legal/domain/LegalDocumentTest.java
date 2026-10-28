package net.akive.api.legal.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEGAL_DOCUMENT_DATE_RANGE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LEGAL_DOCUMENT_EXPIRATION_ALREADY_SET;
import static net.akive.api.legal.domain.LegalDocumentType.SERVICE_TERMS;
import static net.akive.api.legal.domain.LegalInteractionType.AGREEMENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LegalDocumentTest {

    @Test
    void 저장된_약관_및_정책_문서를_복원한다() {
        LocalDate expiresOn = EFFECTIVE_ON.plusDays(1);

        LegalDocument legalDocument = LegalDocument.reconstruct(
                LEGAL_DOCUMENT_ID,
                SERVICE_TERMS,
                VERSION,
                TITLE,
                AGREEMENT,
                false,
                CONTENT,
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                expiresOn,
                CREATED_AT
        );

        assertThat(legalDocument.getLegalDocumentId()).isEqualTo(LEGAL_DOCUMENT_ID);
        assertThat(legalDocument.getDocumentType()).isEqualTo(SERVICE_TERMS);
        assertThat(legalDocument.getVersion()).isEqualTo(VERSION);
        assertThat(legalDocument.getTitle()).isEqualTo(TITLE);
        assertThat(legalDocument.getInteractionType()).isEqualTo(AGREEMENT);
        assertThat(legalDocument.isOptional()).isFalse();
        assertThat(legalDocument.getContent()).isEqualTo(CONTENT);
        assertThat(legalDocument.getAnnouncedOn()).isEqualTo(ANNOUNCED_ON);
        assertThat(legalDocument.getEffectiveOn()).isEqualTo(EFFECTIVE_ON);
        assertThat(legalDocument.getExpiresOn()).isEqualTo(expiresOn);
        assertThat(legalDocument.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 약관_및_정책_문서를_생성한다() {
        LegalDocument legalDocument = LegalDocument.create(
                SERVICE_TERMS,
                VERSION,
                TITLE,
                AGREEMENT,
                false,
                CONTENT,
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                null
        );

        assertThat(legalDocument.getLegalDocumentId()).isNull();
        assertThat(legalDocument.getDocumentType()).isEqualTo(SERVICE_TERMS);
        assertThat(legalDocument.getVersion()).isEqualTo(VERSION);
        assertThat(legalDocument.getTitle()).isEqualTo(TITLE);
        assertThat(legalDocument.getInteractionType()).isEqualTo(AGREEMENT);
        assertThat(legalDocument.isOptional()).isFalse();
        assertThat(legalDocument.getContent()).isEqualTo(CONTENT);
        assertThat(legalDocument.getAnnouncedOn()).isEqualTo(ANNOUNCED_ON);
        assertThat(legalDocument.getEffectiveOn()).isEqualTo(EFFECTIVE_ON);
        assertThat(legalDocument.getExpiresOn()).isNull();
        assertThat(legalDocument.getCreatedAt()).isNull();
    }

    @Test
    void 공고일이_없는_약관_및_정책_문서를_생성할_수_있다() {
        LegalDocument legalDocument = createLegalDocumentWithDates(null, EFFECTIVE_ON, null);

        assertThat(legalDocument.getAnnouncedOn()).isNull();
    }

    @Test
    void 공고일은_시행일보다_늦을_수_없다() {
        assertInvalidDateRange(() -> createLegalDocumentWithDates(
                EFFECTIVE_ON.plusDays(1),
                EFFECTIVE_ON,
                null
        ));
    }

    @ParameterizedTest
    @MethodSource("invalidExpirationDates")
    void 만료일은_시행일과_같거나_빠를_수_없다(LocalDate expiresOn) {
        assertInvalidDateRange(() -> createLegalDocumentWithDates(
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                expiresOn
        ));
    }

    @Test
    void 약관_및_정책_문서에_만료일을_설정한다() {
        LegalDocument legalDocument = createLegalDocument();
        LocalDate expiresOn = EFFECTIVE_ON.plusDays(1);

        legalDocument.expireOn(expiresOn);

        assertThat(legalDocument.getExpiresOn()).isEqualTo(expiresOn);
    }

    @Test
    void 시행일보다_늦지_않은_날짜로_약관_및_정책_문서를_만료_시킬_수_없다() {
        LegalDocument legalDocument = createLegalDocument();

        assertInvalidDateRange(() -> legalDocument.expireOn(EFFECTIVE_ON));
        assertThat(legalDocument.getExpiresOn()).isNull();
    }

    @Test
    void 이미_만료일이_설정된_약관_및_정책_문서의_만료일은_다시_설정할_수_없다() {
        LocalDate expiresOn = EFFECTIVE_ON.plusDays(1);
        LegalDocument legalDocument = createLegalDocumentWithDates(
                ANNOUNCED_ON,
                EFFECTIVE_ON,
                expiresOn
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocument.expireOn(expiresOn.plusDays(1)))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LEGAL_DOCUMENT_EXPIRATION_ALREADY_SET);
    }

    @Test
    void 같은_식별자를_가진_약관_및_정책_문서는_같게_취급한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        LegalDocument sameLegalDocument = reconstructLegalDocument(LEGAL_DOCUMENT_ID);

        assertThat(legalDocument).isEqualTo(sameLegalDocument);
        assertThat(legalDocument.hashCode()).isEqualTo(sameLegalDocument.hashCode());
    }

    @Test
    void 식별자가_다른_약관_및_정책_문서는_다르게_취급한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        LegalDocument anotherLegalDocument = reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID);

        assertThat(legalDocument).isNotEqualTo(anotherLegalDocument);
    }

    @Test
    void 식별자가_없는_약관_및_정책_문서끼리는_나머지_값이_같더라도_다르게_취급한다() {
        LegalDocument legalDocument = createLegalDocument();
        LegalDocument anotherLegalDocument = createLegalDocument();

        assertThat(legalDocument).isNotEqualTo(anotherLegalDocument);
    }

    private static LegalDocument createLegalDocumentWithDates(
            LocalDate announcedOn,
            LocalDate effectiveOn,
            LocalDate expiresOn
    ) {
        return LegalDocument.create(
                SERVICE_TERMS,
                VERSION,
                TITLE,
                AGREEMENT,
                false,
                CONTENT,
                announcedOn,
                effectiveOn,
                expiresOn
        );
    }

    private static Stream<LocalDate> invalidExpirationDates() {
        return Stream.of(
                EFFECTIVE_ON.minusDays(1),
                EFFECTIVE_ON
        );
    }

    private static void assertInvalidDateRange(Runnable runnable) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(runnable::run)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEGAL_DOCUMENT_DATE_RANGE);
    }
}
