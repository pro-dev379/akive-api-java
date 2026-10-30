package net.akive.api.legal.adapter.webapi;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_ACTION_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.LegalDocumentActionType.AGREED;
import static net.akive.api.legal.domain.LegalInteractionType.CONSENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(LegalDocumentActionApi.class)
class LegalDocumentActionApiTest extends PrivateApiTestSupport {

    private static final String CURRENT_LEGAL_DOCUMENT_ACTION_URL =
            "/legal-documents/{legalDocumentId}/legal-document-actions/current";

    @MockitoBean
    LegalDocumentActionFinder legalDocumentActionFinder;

    @Test
    void 회원의_현재_약관_및_정책_문서_응답을_조회한다() {
        LegalDocumentAction legalDocumentAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                CONSENT,
                AGREED
        );
        given(legalDocumentActionFinder.findCurrentOrThrow(USER_ID, LEGAL_DOCUMENT_ID))
                .willReturn(legalDocumentAction);

        MvcTestResultAssert result = get(
                CURRENT_LEGAL_DOCUMENT_ACTION_URL,
                USER_ID,
                LEGAL_DOCUMENT_ID
        ).hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.legalDocumentActionId")
                .isEqualTo(LEGAL_DOCUMENT_ACTION_ID.intValue());
        result.bodyJson()
                .extractingPath("$.legalDocumentId")
                .isEqualTo(LEGAL_DOCUMENT_ID.intValue());
        result.bodyJson()
                .extractingPath("$.actionType")
                .isEqualTo(AGREED.name());
        result.bodyJson()
                .extractingPath("$.context")
                .isEqualTo(SIGN_UP.name());
        result.bodyJson()
                .extractingPath("$.actedAt")
                .isEqualTo(ACTED_AT.toString());
        then(legalDocumentActionFinder).should().findCurrentOrThrow(USER_ID, LEGAL_DOCUMENT_ID);
    }

    @Test
    void 회원의_현재_약관_및_정책_문서_응답이_없으면_404를_응답한다() {
        given(legalDocumentActionFinder.findCurrentOrThrow(USER_ID, LEGAL_DOCUMENT_ID))
                .willThrow(new ResourceNotFoundException(LEGAL_DOCUMENT_ACTION_NOT_FOUND));

        MvcTestResultAssert result = get(
                CURRENT_LEGAL_DOCUMENT_ACTION_URL,
                USER_ID,
                LEGAL_DOCUMENT_ID
        ).hasStatus(NOT_FOUND);

        assertErrorResponse(result, LEGAL_DOCUMENT_ACTION_NOT_FOUND);
        then(legalDocumentActionFinder).should().findCurrentOrThrow(USER_ID, LEGAL_DOCUMENT_ID);
    }
}
