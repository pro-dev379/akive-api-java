package net.akive.api.legal.adapter.webapi;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.CURRENT_LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.LegalDocumentType.SERVICE_TERMS;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(LegalDocumentApi.class)
@AutoConfigureMockMvc(addFilters = false)
class LegalDocumentApiTest extends PublicApiTestSupport {

    private static final String LEGAL_DOCUMENTS_URL = "/legal-documents";
    private static final String CURRENT_LEGAL_DOCUMENT_HEADERS_URL =
            LEGAL_DOCUMENTS_URL + "/action-contexts/{actionContext}/current";
    private static final String CURRENT_LEGAL_DOCUMENT_ID_URL =
            LEGAL_DOCUMENTS_URL + "/types/{documentType}/current";

    @MockitoBean
    LegalDocumentFinder legalDocumentFinder;

    @Test
    void 약관_및_정책_문서_상세를_조회한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(legalDocumentFinder.findOrThrow(LEGAL_DOCUMENT_ID)).willReturn(legalDocument);

        MvcTestResultAssert result = mvcTester.get()
                .uri(LEGAL_DOCUMENTS_URL + "/{legalDocumentId}", LEGAL_DOCUMENT_ID)
                .assertThat()
                .hasStatus(OK);

        assertLegalDocumentResponse(result, legalDocument);
        then(legalDocumentFinder).should().findOrThrow(LEGAL_DOCUMENT_ID);
    }

    @Test
    void 약관_및_정책_식별자에_해당하는_약관_및_정책_문서가_없으면_404를_응답한다() {
        given(legalDocumentFinder.findOrThrow(LEGAL_DOCUMENT_ID))
                .willThrow(new ResourceNotFoundException(LEGAL_DOCUMENT_NOT_FOUND));

        MvcTestResultAssert result = mvcTester.get()
                .uri(LEGAL_DOCUMENTS_URL + "/{legalDocumentId}", LEGAL_DOCUMENT_ID)
                .assertThat()
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, LEGAL_DOCUMENT_NOT_FOUND);
        then(legalDocumentFinder).should().findOrThrow(LEGAL_DOCUMENT_ID);
    }

    @Test
    void 특정_타입의_현행_약관_및_정책_문서_식별자를_조회한다() {
        given(legalDocumentFinder.findCurrentIdByDocumentType(SERVICE_TERMS))
                .willReturn(LEGAL_DOCUMENT_ID);

        MvcTestResultAssert result = mvcTester.get()
                .uri(CURRENT_LEGAL_DOCUMENT_ID_URL, SERVICE_TERMS)
                .assertThat()
                .hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.legalDocumentId")
                .isEqualTo(LEGAL_DOCUMENT_ID.intValue());
        then(legalDocumentFinder).should().findCurrentIdByDocumentType(SERVICE_TERMS);
    }

    @Test
    void 현행_약관_및_정책_문서_식별자_조회_시_약관_및_정책_문서_타입이_올바르지_않으면_400을_응답한다() {
        mvcTester.get()
                .uri(CURRENT_LEGAL_DOCUMENT_ID_URL, "UNKNOWN")
                .assertThat()
                .hasStatus(BAD_REQUEST);

        then(legalDocumentFinder).shouldHaveNoInteractions();
    }

    @Test
    void 특정_타입의_현행_약관_및_정책_문서가_없으면_404를_응답한다() {
        given(legalDocumentFinder.findCurrentIdByDocumentType(SERVICE_TERMS))
                .willThrow(new ResourceNotFoundException(CURRENT_LEGAL_DOCUMENT_NOT_FOUND));

        MvcTestResultAssert result = mvcTester.get()
                .uri(CURRENT_LEGAL_DOCUMENT_ID_URL, SERVICE_TERMS)
                .assertThat()
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, CURRENT_LEGAL_DOCUMENT_NOT_FOUND);
        then(legalDocumentFinder).should().findCurrentIdByDocumentType(SERVICE_TERMS);
    }

    @Test
    void 응답_컨텍스트에_맞는_현행_약관_및_정책_문서_헤더_목록을_조회한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP))
                .willReturn(List.of(legalDocument));

        MvcTestResultAssert result = mvcTester.get()
                .uri(CURRENT_LEGAL_DOCUMENT_HEADERS_URL, SIGN_UP)
                .assertThat()
                .hasStatus(OK);

        assertLegalDocumentHeaderResponse(result, legalDocument);

        then(legalDocumentFinder).should().findAllCurrentByContext(SIGN_UP);
    }

    @Test
    void 현행_약관_및_정책_문서_헤더_목록_조회_시_응답_컨텍스트가_올바르지_않으면_400을_응답한다() {
        mvcTester.get()
                .uri(CURRENT_LEGAL_DOCUMENT_HEADERS_URL, "UNKNOWN")
                .assertThat()
                .hasStatus(BAD_REQUEST);

        then(legalDocumentFinder).shouldHaveNoInteractions();
    }

    private void assertLegalDocumentHeaderResponse(MvcTestResultAssert result, LegalDocument legalDocument) {
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].legalDocumentId")
                .isEqualTo(legalDocument.getLegalDocumentId().intValue());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].documentType")
                .isEqualTo(legalDocument.getDocumentType().name());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].version")
                .isEqualTo(legalDocument.getVersion());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].title")
                .isEqualTo(legalDocument.getTitle());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].interactionType")
                .isEqualTo(legalDocument.getInteractionType().name());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].isOptional")
                .isEqualTo(legalDocument.isOptional());
        result.bodyJson()
                .extractingPath("$.legalDocumentHeaders[0].effectiveOn")
                .isEqualTo(legalDocument.getEffectiveOn().toString());
    }

    private void assertLegalDocumentResponse(MvcTestResultAssert result, LegalDocument legalDocument) {
        result.bodyJson()
                .extractingPath("$.legalDocumentId")
                .isEqualTo(legalDocument.getLegalDocumentId().intValue());
        result.bodyJson()
                .extractingPath("$.documentType")
                .isEqualTo(legalDocument.getDocumentType().name());
        result.bodyJson()
                .extractingPath("$.version")
                .isEqualTo(legalDocument.getVersion());
        result.bodyJson()
                .extractingPath("$.title")
                .isEqualTo(legalDocument.getTitle());
        result.bodyJson()
                .extractingPath("$.interactionType")
                .isEqualTo(legalDocument.getInteractionType().name());
        result.bodyJson()
                .extractingPath("$.isOptional")
                .isEqualTo(legalDocument.isOptional());
        result.bodyJson()
                .extractingPath("$.content")
                .isEqualTo(legalDocument.getContent());
        result.bodyJson()
                .extractingPath("$.announcedOn")
                .isEqualTo(legalDocument.getAnnouncedOn().toString());
        result.bodyJson()
                .extractingPath("$.effectiveOn")
                .isEqualTo(legalDocument.getEffectiveOn().toString());
        result.bodyJson()
                .extractingPath("$.expiresOn")
                .isNull();
    }
}
