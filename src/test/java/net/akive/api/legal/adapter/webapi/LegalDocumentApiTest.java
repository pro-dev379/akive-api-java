package net.akive.api.legal.adapter.webapi;

import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.reconstructLegalDocument;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(LegalDocumentApi.class)
@AutoConfigureMockMvc(addFilters = false)
class LegalDocumentApiTest extends PublicApiTestSupport {

    private static final String LEGAL_DOCUMENTS_URL = "/legal-documents";

    @MockitoBean
    LegalDocumentFinder legalDocumentFinder;

    @Test
    void 현행_약관_및_정책_문서_목록을_조회한다() {
        LegalDocument legalDocument = reconstructLegalDocument();
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP))
                .willReturn(List.of(legalDocument));

        MvcTestResultAssert result = mvcTester.get()
                .uri(LEGAL_DOCUMENTS_URL + "?context={context}", SIGN_UP)
                .assertThat()
                .hasStatus(OK);

        assertLegalDocumentResponse(result, legalDocument);

        then(legalDocumentFinder).should().findAllCurrentByContext(SIGN_UP);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?context=UNKNOWN"})
    void 현행_약관_및_정책_문서_목록_조회_시_응답_컨텍스트_요청값이_올바르지_않으면_400을_응답한다(String queryString) {
        mvcTester.get()
                .uri(LEGAL_DOCUMENTS_URL + queryString)
                .assertThat()
                .hasStatus(BAD_REQUEST);

        then(legalDocumentFinder).shouldHaveNoInteractions();
    }

    private void assertLegalDocumentResponse(MvcTestResultAssert result, LegalDocument legalDocument) {
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].legalDocumentId")
                .isEqualTo(legalDocument.getLegalDocumentId().intValue());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].documentType")
                .isEqualTo(legalDocument.getDocumentType().name());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].version")
                .isEqualTo(legalDocument.getVersion());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].title")
                .isEqualTo(legalDocument.getTitle());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].interactionType")
                .isEqualTo(legalDocument.getInteractionType().name());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].isOptional")
                .isEqualTo(legalDocument.isOptional());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].content")
                .isEqualTo(legalDocument.getContent());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].announcedOn")
                .isEqualTo(legalDocument.getAnnouncedOn().toString());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].effectiveOn")
                .isEqualTo(legalDocument.getEffectiveOn().toString());
        result.bodyJson()
                .extractingPath("$.legalDocuments[0].expiresOn")
                .isNull();
    }
}
