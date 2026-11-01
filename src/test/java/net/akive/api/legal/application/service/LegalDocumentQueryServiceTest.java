package net.akive.api.legal.application.service;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.legal.application.required.LegalDocumentRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.legal.domain.LegalDocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.ResourceNotFoundError.CURRENT_LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.*;
import static net.akive.api.legal.domain.LegalDocumentType.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LegalDocumentQueryServiceTest {

    private static final Instant CURRENT_INSTANT = Instant.parse("2026-10-28T15:00:00Z");
    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 10, 29);
    private static final List<LegalDocumentType> SIGN_UP_DOCUMENT_TYPES = List.of(
            SERVICE_TERMS,
            PRIVACY_POLICY,
            AGE_14_CONFIRMATION,
            POSTHOG_ANALYTICS,
            POSTHOG_OVERSEAS_TRANSFER
    );
    private static final List<LegalDocumentType> AI_FIRST_USE_DOCUMENT_TYPES = List.of(
            AI_LIMITATIONS,
            AI_PERSONAL_DATA,
            OPENAI_OVERSEAS_TRANSFER
    );
    private static final List<LegalDocumentType> CONSENT_MANAGEMENT_DOCUMENT_TYPES = List.of(
            POSTHOG_ANALYTICS,
            POSTHOG_OVERSEAS_TRANSFER,
            AI_PERSONAL_DATA,
            OPENAI_OVERSEAS_TRANSFER
    );

    @Mock
    LegalDocumentRepository legalDocumentRepository;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    LegalDocumentQueryService legalDocumentQueryService;

    @Test
    void 약관_및_정책_식별자로_약관_및_정책_문서를_조회한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(legalDocumentRepository.findById(LEGAL_DOCUMENT_ID))
                .willReturn(Optional.of(legalDocument));

        LegalDocument foundLegalDocument = legalDocumentQueryService.findOrThrow(LEGAL_DOCUMENT_ID);

        assertThat(foundLegalDocument).isEqualTo(legalDocument);
        then(legalDocumentRepository).should().findById(LEGAL_DOCUMENT_ID);
    }

    @Test
    void 약관_및_정책_식별자로_약관_및_정책_문서를_조회할_수_없으면_예외를_던진다() {
        given(legalDocumentRepository.findById(UNKNOWN_LEGAL_DOCUMENT_ID))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> legalDocumentQueryService.findOrThrow(UNKNOWN_LEGAL_DOCUMENT_ID))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(LEGAL_DOCUMENT_NOT_FOUND);

        then(legalDocumentRepository).should().findById(UNKNOWN_LEGAL_DOCUMENT_ID);
    }

    @Test
    void 특정_타입의_현행_약관_및_정책_문서를_조회한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE))
                .willReturn(List.of(legalDocument));

        LegalDocument foundLegalDocument = legalDocumentQueryService.findCurrentByDocumentType(SERVICE_TERMS);

        assertThat(foundLegalDocument).isEqualTo(legalDocument);
        then(legalDocumentRepository).should()
                .findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE);
    }

    @Test
    void 특정_타입의_현행_약관_및_정책_문서가_없으면_예외를_던진다() {
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE))
                .willReturn(List.of());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> legalDocumentQueryService.findCurrentByDocumentType(SERVICE_TERMS))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(CURRENT_LEGAL_DOCUMENT_NOT_FOUND);

        then(legalDocumentRepository).should()
                .findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE);
    }

    @Test
    void 특정_타입의_현행_약관_및_정책_문서가_여러_개면_예외를_던진다() {
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE))
                .willReturn(List.of(
                        reconstructLegalDocument(LEGAL_DOCUMENT_ID),
                        reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID)
                ));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> legalDocumentQueryService.findCurrentByDocumentType(SERVICE_TERMS))
                .withMessage("Multiple current legal documents found for type: %s", SERVICE_TERMS);

        then(legalDocumentRepository).should()
                .findAllCurrentByDocumentTypeIn(List.of(SERVICE_TERMS), CURRENT_DATE);
    }

    @ParameterizedTest
    @MethodSource("contextAndDocumentTypes")
    void 응답_컨텍스트에_맞는_현행_약관_및_정책_문서를_정해진_순서대로_조회한다(
            LegalDocumentActionContext context,
            List<LegalDocumentType> documentTypes
    ) {
        List<LegalDocument> legalDocuments = reconstructLegalDocuments(documentTypes);
        List<LegalDocument> unorderedLegalDocuments = new ArrayList<>(legalDocuments);
        Collections.reverse(unorderedLegalDocuments);
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(documentTypes, CURRENT_DATE))
                .willReturn(unorderedLegalDocuments);

        List<LegalDocument> foundLegalDocuments = legalDocumentQueryService.findAllCurrentByContext(context);

        assertThat(foundLegalDocuments).containsExactlyElementsOf(legalDocuments);
        then(legalDocumentRepository).should().findAllCurrentByDocumentTypeIn(documentTypes, CURRENT_DATE);
    }

    @Test
    void 응답_컨텍스트에_맞는_현행_약관_및_정책_문서가_없으면_예외를_던진다() {
        List<LegalDocument> legalDocuments = reconstructLegalDocuments(SIGN_UP_DOCUMENT_TYPES).stream()
                .filter(legalDocument -> legalDocument.getDocumentType() != PRIVACY_POLICY)
                .toList();
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(SIGN_UP_DOCUMENT_TYPES, CURRENT_DATE))
                .willReturn(legalDocuments);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> legalDocumentQueryService.findAllCurrentByContext(SIGN_UP))
                .withMessage(
                        "Current legal document not found: context=%s, documentType=%s",
                        SIGN_UP,
                        PRIVACY_POLICY
                );
    }

    @Test
    void 응답_컨텍스트에_같은_타입의_현행_약관_및_정책_문서가_여러_개면_예외를_던진다() {
        LegalDocument legalDocument = reconstructLegalDocument(LEGAL_DOCUMENT_ID, SERVICE_TERMS);
        LegalDocument anotherLegalDocument = reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID, SERVICE_TERMS);
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllCurrentByDocumentTypeIn(SIGN_UP_DOCUMENT_TYPES, CURRENT_DATE))
                .willReturn(List.of(legalDocument, anotherLegalDocument));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> legalDocumentQueryService.findAllCurrentByContext(SIGN_UP))
                .withMessage("Multiple current legal documents found for type: %s", SERVICE_TERMS);
    }

    @Test
    void 특정_타입의_시행_약관_및_정책_문서_이력을_조회한다() {
        List<LegalDocument> legalDocuments = List.of(
                reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID),
                reconstructLegalDocument(LEGAL_DOCUMENT_ID)
        );
        given(currentTimeProvider.now()).willReturn(CURRENT_INSTANT);
        given(legalDocumentRepository.findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
                SERVICE_TERMS,
                CURRENT_DATE
        )).willReturn(legalDocuments);

        List<LegalDocument> foundLegalDocuments =
                legalDocumentQueryService.findAllHistoryByDocumentType(SERVICE_TERMS);

        assertThat(foundLegalDocuments).containsExactlyElementsOf(legalDocuments);
        then(legalDocumentRepository).should().findAllHistoryByDocumentTypeOrderByEffectiveOnDesc(
                SERVICE_TERMS,
                CURRENT_DATE
        );
    }

    private static Stream<Arguments> contextAndDocumentTypes() {
        return Stream.of(
                Arguments.of(SIGN_UP, SIGN_UP_DOCUMENT_TYPES),
                Arguments.of(EXISTING_USER_ONBOARDING, SIGN_UP_DOCUMENT_TYPES),
                Arguments.of(AI_FIRST_USE, AI_FIRST_USE_DOCUMENT_TYPES),
                Arguments.of(CONSENT_MANAGEMENT, CONSENT_MANAGEMENT_DOCUMENT_TYPES)
        );
    }
}
