package net.akive.api.legal.adapter.webapi;

import net.akive.api.common.exception.business.*;
import net.akive.api.legal.adapter.webapi.dto.request.LegalDocumentActionRecordRequest;
import net.akive.api.legal.adapter.webapi.dto.request.LegalDocumentActionsRecordRequest;
import net.akive.api.legal.application.provided.LegalDocumentActionFinder;
import net.akive.api.legal.application.provided.LegalDocumentActionRecorder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceConflictError.LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT;
import static net.akive.api.common.exception.business.ResourceNotFoundError.LEGAL_DOCUMENT_ACTION_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.SIGN_UP;
import static net.akive.api.legal.domain.LegalDocumentActionType.AGREED;
import static net.akive.api.legal.domain.LegalInteractionType.CONSENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(LegalDocumentActionApi.class)
class LegalDocumentActionApiTest extends PrivateApiTestSupport {

    private static final String CURRENT_LEGAL_DOCUMENT_ACTION_URL =
            "/legal-documents/{legalDocumentId}/legal-document-actions/current";
    private static final String LEGAL_DOCUMENT_ACTIONS_URL = "/legal-document-actions";

    @MockitoBean
    LegalDocumentActionFinder legalDocumentActionFinder;

    @MockitoBean
    LegalDocumentActionRecorder legalDocumentActionRecorder;

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

    @Test
    void 가입_후_약관_및_정책_문서에_대한_회원_응답을_기록한다() {
        LegalDocumentActionsRecordRequest request = createLegalDocumentActionsRecordRequest();
        LegalDocumentActionsRecordCommand command = request.to(USER_ID);

        postJson(LEGAL_DOCUMENT_ACTIONS_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(legalDocumentActionRecorder).should().record(command);
    }

    @Test
    void 가입_후_약관_및_정책_문서에_대한_회원_응답이_없으면_400을_응답한다() {
        LegalDocumentActionsRecordRequest request = new LegalDocumentActionsRecordRequest(SIGN_UP, List.of());

        postJson(LEGAL_DOCUMENT_ACTIONS_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(legalDocumentActionRecorder).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(
            value = BusinessRuleViolationError.class,
            names = {
                    "DUPLICATE_LEGAL_DOCUMENT_ACTION_REQUEST",
                    "INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT",
                    "REQUIRED_LEGAL_DOCUMENT_ACTION_MISSING"
            }
    )
    void 약관_및_정책_문서에_대한_회원_응답_기록_규칙을_위반하면_400을_응답한다(BusinessRuleViolationError error) {
        LegalDocumentActionsRecordRequest request = createLegalDocumentActionsRecordRequest();
        LegalDocumentActionsRecordCommand command = request.to(USER_ID);
        willThrow(new BusinessRuleViolationException(error))
                .given(legalDocumentActionRecorder)
                .record(command);

        MvcTestResultAssert result = postJson(LEGAL_DOCUMENT_ACTIONS_URL, USER_ID, toJson(request))
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, error);
        then(legalDocumentActionRecorder).should().record(command);
    }

    @ParameterizedTest
    @EnumSource(
            value = ResourceNotFoundError.class,
            names = {"USER_NOT_FOUND", "LEGAL_DOCUMENT_NOT_FOUND", "CURRENT_LEGAL_DOCUMENT_NOT_FOUND"}
    )
    void 약관_및_정책_문서에_대한_회원_응답_기록에_필요한_리소스를_찾을_수_없으면_404를_응답한다(ResourceNotFoundError error) {
        LegalDocumentActionsRecordRequest request = createLegalDocumentActionsRecordRequest();
        LegalDocumentActionsRecordCommand command = request.to(USER_ID);
        willThrow(new ResourceNotFoundException(error))
                .given(legalDocumentActionRecorder)
                .record(command);

        MvcTestResultAssert result = postJson(LEGAL_DOCUMENT_ACTIONS_URL, USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, error);
        then(legalDocumentActionRecorder).should().record(command);
    }

    @Test
    void 동일한_요청_식별자로_서로_다른_회원_응답을_기록하면_409를_응답한다() {
        LegalDocumentActionsRecordRequest request = createLegalDocumentActionsRecordRequest();
        LegalDocumentActionsRecordCommand command = request.to(USER_ID);
        willThrow(new ResourceConflictException(LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT))
                .given(legalDocumentActionRecorder)
                .record(command);

        MvcTestResultAssert result = postJson(LEGAL_DOCUMENT_ACTIONS_URL, USER_ID, toJson(request))
                .hasStatus(CONFLICT);

        assertErrorResponse(result, LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT);
        then(legalDocumentActionRecorder).should().record(command);
    }

    private LegalDocumentActionsRecordRequest createLegalDocumentActionsRecordRequest() {
        return new LegalDocumentActionsRecordRequest(
                SIGN_UP,
                List.of(new LegalDocumentActionRecordRequest(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED))
        );
    }
}
