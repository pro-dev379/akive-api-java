package net.akive.api.legal.application.service;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionRecordItem;
import net.akive.api.legal.application.provided.dto.LegalDocumentActionsRecordCommand;
import net.akive.api.legal.application.required.LegalDocumentActionRepository;
import net.akive.api.legal.domain.LegalDocument;
import net.akive.api.legal.domain.LegalDocumentAction;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.common.exception.business.ResourceConflictError.LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT;
import static net.akive.api.common.exception.business.ResourceNotFoundError.CURRENT_LEGAL_DOCUMENT_NOT_FOUND;
import static net.akive.api.legal.domain.LegalDocumentActionContext.*;
import static net.akive.api.legal.domain.LegalDocumentActionType.AGREED;
import static net.akive.api.legal.domain.LegalDocumentActionType.DECLINED;
import static net.akive.api.legal.domain.LegalDocumentType.*;
import static net.akive.api.legal.domain.LegalInteractionType.AGREEMENT;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.*;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LegalDocumentActionCommandServiceTest {

    @Mock
    UserFinder userFinder;

    @Mock
    LegalDocumentFinder legalDocumentFinder;

    @Mock
    LegalDocumentActionRepository legalDocumentActionRepository;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    LegalDocumentActionCommandService legalDocumentActionCommandService;

    @Test
    void 약관_및_정책_문서에_대한_회원_응답을_기록한다() {
        User user = createReconstructedUser();
        LegalDocument signUpLegalDocument = reconstructLegalDocument(LEGAL_DOCUMENT_ID, SERVICE_TERMS);
        LegalDocumentActionsRecordCommand command = createSignUpCommand(
                new LegalDocumentActionRecordItem(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED)
        );
        givenUnrecordedActionRequest(user, signUpLegalDocument);
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP))
                .willReturn(List.of(signUpLegalDocument));
        given(currentTimeProvider.now()).willReturn(ACTED_AT);

        legalDocumentActionCommandService.record(command);

        ArgumentCaptor<LegalDocumentAction> actionCaptor = ArgumentCaptor.forClass(LegalDocumentAction.class);
        then(legalDocumentActionRepository).should().save(actionCaptor.capture());
        LegalDocumentAction legalDocumentAction = actionCaptor.getValue();
        assertThat(legalDocumentAction.getUser()).isEqualTo(user);
        assertThat(legalDocumentAction.getLegalDocument()).isEqualTo(signUpLegalDocument);
        assertThat(legalDocumentAction.getActionType()).isEqualTo(AGREED);
        assertThat(legalDocumentAction.getContext()).isEqualTo(SIGN_UP);
        assertThat(legalDocumentAction.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(legalDocumentAction.getActedAt()).isEqualTo(ACTED_AT);
    }

    @Test
    void 동일한_약관_및_정책_문서에_중복으로_응답하면_예외를_던진다() {
        LegalDocumentActionsRecordCommand command = createSignUpCommand(
                new LegalDocumentActionRecordItem(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED),
                new LegalDocumentActionRecordItem(ANOTHER_REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED)
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DUPLICATE_LEGAL_DOCUMENT_ACTION_REQUEST);

        then(userFinder).shouldHaveNoInteractions();
        then(legalDocumentActionRepository).shouldHaveNoInteractions();
    }

    @Test
    void 응답_컨텍스트에_맞지_않는_약관_및_정책_문서에_응답하면_예외를_던진다() {
        User user = createReconstructedUser();
        LegalDocument requestedLegalDocument = reconstructLegalDocument(ANOTHER_LEGAL_DOCUMENT_ID, AI_LIMITATIONS);
        LegalDocument signUpLegalDocument = reconstructLegalDocument(LEGAL_DOCUMENT_ID, SERVICE_TERMS);
        LegalDocumentActionsRecordCommand command = createSignUpCommand(
                new LegalDocumentActionRecordItem(REQUEST_ID, ANOTHER_LEGAL_DOCUMENT_ID, AGREED)
        );
        givenUnrecordedActionRequest(user, requestedLegalDocument);
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP))
                .willReturn(List.of(signUpLegalDocument));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);

        then(legalDocumentActionRepository).should().findByRequestId(REQUEST_ID);
        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 필수_약관_및_정책_문서에_응답하지_않으면_예외를_던진다() {
        User user = createReconstructedUser();
        LegalDocument requestedSignUpLegalDocument = reconstructLegalDocument(
                LEGAL_DOCUMENT_ID,
                SERVICE_TERMS
        );
        LegalDocument missingRequiredSignUpLegalDocument = reconstructLegalDocument(
                ANOTHER_LEGAL_DOCUMENT_ID,
                PRIVACY_POLICY
        );
        LegalDocumentActionsRecordCommand command = createSignUpCommand(
                new LegalDocumentActionRecordItem(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED)
        );
        givenUnrecordedActionRequest(user, requestedSignUpLegalDocument);
        given(legalDocumentFinder.findAllCurrentByContext(SIGN_UP)).willReturn(List.of(
                requestedSignUpLegalDocument,
                missingRequiredSignUpLegalDocument
        ));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_LEGAL_DOCUMENT_ACTION_MISSING);

        then(legalDocumentActionRepository).should().findByRequestId(REQUEST_ID);
        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @ParameterizedTest
    @EnumSource(
            value = LegalDocumentActionContext.class,
            names = {"CONSENT_MANAGEMENT", "POLICY_UPDATE"}
    )
    void 현행이_아닌_약관_및_정책_문서에_응답하면_예외를_던진다(LegalDocumentActionContext actionContext) {
        Long nonCurrentLegalDocumentId = 1L;
        Long currentLegalDocumentId = 2L;
        LegalDocument nonCurrentLegalDocument = reconstructLegalDocument(
                nonCurrentLegalDocumentId,
                SERVICE_TERMS
        );
        LegalDocumentActionsRecordCommand command = createCommand(
                actionContext,
                new LegalDocumentActionRecordItem(REQUEST_ID, nonCurrentLegalDocumentId, AGREED)
        );
        givenUnrecordedActionRequest(createReconstructedUser(), nonCurrentLegalDocument);
        given(legalDocumentFinder.findCurrentByDocumentType(SERVICE_TERMS))
                .willReturn(reconstructLegalDocument(currentLegalDocumentId, SERVICE_TERMS));

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);

        then(legalDocumentActionRepository).should().findByRequestId(REQUEST_ID);
        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void CONSENT_MANAGEMENT_컨텍스트에서_CONSENT_타입이_아닌_약관_및_정책_문서에_응답하면_예외를_던진다() {
        User user = createReconstructedUser();
        LegalDocument agreementLegalDocument = reconstructLegalDocument(AGREEMENT);
        LegalDocumentActionsRecordCommand command = createCommand(
                CONSENT_MANAGEMENT,
                new LegalDocumentActionRecordItem(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED)
        );
        givenUnrecordedActionRequest(user, agreementLegalDocument);
        given(legalDocumentFinder.findCurrentByDocumentType(SERVICE_TERMS))
                .willReturn(agreementLegalDocument);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEGAL_DOCUMENT_ACTION_FOR_CONTEXT);

        then(legalDocumentActionRepository).should().findByRequestId(REQUEST_ID);
        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 현행_약관_및_정책_문서를_찾을_수_없으면_예외를_던진다() {
        User user = createReconstructedUser();
        LegalDocument requestedLegalDocument = reconstructLegalDocument(LEGAL_DOCUMENT_ID, SERVICE_TERMS);
        LegalDocumentActionsRecordCommand command = createCommand(
                POLICY_UPDATE,
                new LegalDocumentActionRecordItem(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED)
        );
        givenUnrecordedActionRequest(user, requestedLegalDocument);
        given(legalDocumentFinder.findCurrentByDocumentType(SERVICE_TERMS))
                .willThrow(new ResourceNotFoundException(CURRENT_LEGAL_DOCUMENT_NOT_FOUND));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(ResourceNotFoundException::getError)
                .isEqualTo(CURRENT_LEGAL_DOCUMENT_NOT_FOUND);

        then(legalDocumentActionRepository).should().findByRequestId(REQUEST_ID);
        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 동일한_요청으로_회원_응답_기록을_재시도하면_기존_응답을_유지한다() {
        LegalDocumentAction existingAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                AGREEMENT,
                AGREED
        );
        LegalDocumentActionsRecordCommand command = new LegalDocumentActionsRecordCommand(
                existingAction.getUser().getUserId(),
                existingAction.getContext(),
                List.of(new LegalDocumentActionRecordItem(
                        existingAction.getRequestId(),
                        existingAction.getLegalDocument().getLegalDocumentId(),
                        existingAction.getActionType()
                ))
        );
        givenRecordedAction(existingAction);

        legalDocumentActionCommandService.record(command);

        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void 동일한_요청_식별자로_서로_다른_회원_응답을_기록하면_예외를_던진다() {
        LegalDocumentAction existingAction = reconstructLegalDocumentAction(
                LEGAL_DOCUMENT_ACTION_ID,
                AGREEMENT,
                AGREED
        );
        LegalDocumentActionsRecordCommand command = new LegalDocumentActionsRecordCommand(
                existingAction.getUser().getUserId(),
                existingAction.getContext(),
                List.of(new LegalDocumentActionRecordItem(
                        existingAction.getRequestId(),
                        existingAction.getLegalDocument().getLegalDocumentId(),
                        DECLINED
                ))
        );
        givenRecordedAction(existingAction);

        assertThatExceptionOfType(ResourceConflictException.class)
                .isThrownBy(() -> legalDocumentActionCommandService.record(command))
                .extracting(ResourceConflictException::getError)
                .isEqualTo(LEGAL_DOCUMENT_ACTION_REQUEST_CONFLICT);

        then(legalDocumentActionRepository).shouldHaveNoMoreInteractions();
    }

    private LegalDocumentActionsRecordCommand createSignUpCommand(LegalDocumentActionRecordItem... actions) {
        return createCommand(SIGN_UP, actions);
    }

    private LegalDocumentActionsRecordCommand createCommand(
            LegalDocumentActionContext actionContext,
            LegalDocumentActionRecordItem... actions
    ) {
        return new LegalDocumentActionsRecordCommand(USER_ID, actionContext, List.of(actions));
    }

    private void givenUnrecordedActionRequest(User user, LegalDocument requestedLegalDocument) {
        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(legalDocumentActionRepository.findByRequestId(REQUEST_ID)).willReturn(Optional.empty());
        given(legalDocumentFinder.findOrThrow(requestedLegalDocument.getLegalDocumentId()))
                .willReturn(requestedLegalDocument);
    }

    private void givenRecordedAction(LegalDocumentAction legalDocumentAction) {
        given(userFinder.findOrThrow(USER_ID)).willReturn(legalDocumentAction.getUser());
        given(legalDocumentActionRepository.findByRequestId(legalDocumentAction.getRequestId()))
                .willReturn(Optional.of(legalDocumentAction));
    }
}
